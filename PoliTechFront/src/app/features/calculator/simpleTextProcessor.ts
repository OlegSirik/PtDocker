import { FormulaLine } from '../../shared/services/calculator.service';
import { CalculatorVar } from '../../shared/services/calculator.service';

interface ProcessorConfig {
    validationMode?: 'strict' | 'warn' | 'none';
    variables?: string[];
    compareOps?: string[];
    mathOps?: string[];
    postProcessors?: string[];
    constants?: string[];
}

interface ParsedLine {
    nr: string;
    conditionLeft: string;
    conditionOperator: string;
    conditionRight: string;
    expressionResult: string;
    expressionLeft: string;
    expressionOperator: string;
    expressionRight: string;
    postProcessor: string;
}

interface ParseResult {
    lines: ParsedLine[];
    errors: ErrorInfo[];
    usedVars: string[];
}

interface ErrorInfo {
    type: 'ERROR' | 'WARNING';
    message: string;
    line: number;
    token?: string;
}

interface ExpressionParts {
    left: string;
    op: string;
    right: string;
    postProcessor: string;
}

interface LineStructure {
    type: 'condition' | 'assignment';
    conditionLeft?: string;
    conditionOp?: string;
    conditionRight?: string;
    result?: string;
    expression?: string;
    assignment?: string;
}

export class SimpleScriptProcessor {
    private config: {
        validationMode: 'strict' | 'warn' | 'none';
        dictionary: {
            variables: string[];
            compareOps: string[];
            mathOps: string[];
            postProcessors: string[];
            constants: string[];
        };
        patterns: {
            assignment: RegExp;
            condition: RegExp;
        };
    };
    private errors: ErrorInfo[] = [];
    private usedVars: Set<string> = new Set();

    constructor(config: ProcessorConfig = {}) {
        this.config = {
            validationMode: config.validationMode || 'strict',
            dictionary: {
                variables: config.variables || [
                    'io_sumInsured',
                    'pl_premium',
                    'K_Age',
                    'K_Period',
                    'InsAmount',
                    'Limit',
                    'ACC_InsAmount',
                    'DIS_InsAmount',
                    'DIS_LimitMax',
                    'co_COMPLEX_ACCIDENT_sumInsured',
                    'co_COMPLEX_DISEASE_sumInsured',
                    'co_COMPLEX_DISEASE_limitMax'
                ],
                compareOps: config.compareOps && config.compareOps.length > 0 
                    ? config.compareOps 
                    : ['>', '<', '>=', '<=', '==', '!='],
                mathOps: config.mathOps && config.mathOps.length > 0 
                    ? config.mathOps 
                    : ['+', '-', '*', '/', 'min','max'],
                postProcessors: config.postProcessors && config.postProcessors.length > 0 
                    ? config.postProcessors 
                    : ['round2_up', 'round2', 'round', 'ceil', 'floor', 'abs'],
                constants: config.constants || ['CONST.0', 'CONST.1', 'CONST.100', 'CONST.1000']
            },
            patterns: {
                assignment: /^(\w+(?:\.\w+)*)\s*=\s*(.+)$/,
                condition: /^if\s+(.+)\s+(>|<|>=|<=|=|!=)\s+(.+)\s+then\s+(.+)$/
            }
        };
        
        this.errors = [];
        this.usedVars = new Set();
    }
    
    /**
     * Парсинг скрипта из текста в JSON
     */
    parseScript(text: string): ParseResult {
        this.errors = [];
        this.usedVars.clear();
        
        const lines = text.split('\n')
            .map((line, idx) => {
                const trimmed = line.trim();
                // Если строка начинается с числа и пробела, извлекаем nr
                const nrMatch = trimmed.match(/^(\d+)\s+(.+)$/);
                if (nrMatch) {
                    return {
                        text: nrMatch[2].trim(),
                        lineNum: parseInt(nrMatch[1], 10),
                        originalNr: nrMatch[1]
                    };
                }
                return {
                    text: trimmed,
                    lineNum: idx + 1,
                    originalNr: String(idx + 1)
                };
            })
            .filter(line => line.text && !line.text.startsWith('//'));
        
        const result: ParsedLine[] = [];
        
        for (const {text, lineNum, originalNr} of lines) {
            // Пропускаем строки, если уже есть фатальные ошибки
            if (this.hasFatalErrors()) {
                break;
            }
            
            const parsed = this.parseLineWithValidation(text, lineNum);
            if (parsed) {
                parsed.nr = originalNr;
                result.push(parsed);
            }
        }
        
        return {
            lines: result,
            errors: this.errors,
            usedVars: Array.from(this.usedVars)
        };
    }
    
    /**
     * Парсинг строки с полной валидацией
     */
    private parseLineWithValidation(text: string, lineNum: number): ParsedLine | null {
        // 1. Определяем структуру строки
        const structure = this.determineLineStructure(text, lineNum);
        if (!structure) return null;
        
        // 2. Если есть фатальные ошибки, останавливаемся
        if (this.hasFatalErrors()) return null;
        
        // 3. Парсим в зависимости от типа
        if (structure.type === 'condition') {
            return this.parseAndValidateCondition(
                structure as LineStructure & { type: 'condition' },
                lineNum
            );
        } else {
            return this.parseAndValidateAssignment(
                structure as LineStructure & { type: 'assignment' },
                lineNum
            );
        }
    }
    
    /**
     * Определение структуры строки с валидацией шаблонов
     */
    private determineLineStructure(text: string, lineNum: number): LineStructure | null {
        // Условие: if X > Y then Z = A
        const condMatch = text.match(this.config.patterns.condition);
        if (condMatch) {
            return {
                type: 'condition',
                conditionLeft: condMatch[1].trim(),
                conditionOp: condMatch[2].trim(),
                conditionRight: condMatch[3].trim(),
                assignment: condMatch[4].trim()
            };
        }
        
        // Простое присваивание: X = Y
        const assignMatch = text.match(this.config.patterns.assignment);
        if (assignMatch) {
            return {
                type: 'assignment',
                result: assignMatch[1].trim(),
                expression: assignMatch[2].trim()
            };
        }
        
        // Если ни один шаблон не подошел
        this.addError(lineNum, `Неправильный формат строки: "${text}"`, 'error');
        return null;
    }
    
    /**
     * Парсинг и валидация условия
     */
    private parseAndValidateCondition(
        structure: LineStructure & { type: 'condition' },
        lineNum: number
    ): ParsedLine | null {
        // Извлекаем поля с проверкой на undefined
        const conditionLeft = structure.conditionLeft || '';
        const conditionOp = structure.conditionOp || '';
        const conditionRight = structure.conditionRight || '';
        const assignment = structure.assignment || '';
        
        // 1. Валидация оператора условия
        if (!conditionOp || !this.validateTokenInDictionary(conditionOp, 'conditionOperator', lineNum)) {
            return null;
        }
        
        // 2. Валидация левой части условия
        if (!conditionLeft || !this.validateTokenInDictionary(conditionLeft, 'variableOrConstant', lineNum)) {
            return null;
        }
        
        // 3. Валидация правой части условия
        if (!conditionRight || !this.validateTokenInDictionary(conditionRight, 'variableOrConstant', lineNum)) {
            return null;
        }
        
        // 4. Парсинг присваивания после then
        const assignMatch = assignment.match(this.config.patterns.assignment);
        if (!assignMatch) {
            this.addError(lineNum, `Неправильный формат присваивания в условии: "${assignment}"`, 'error');
            return null;
        }
        
        const result = assignMatch[1].trim();
        const expression = assignMatch[2].trim();
        
        // 5. Валидация результата присваивания
        if (!result || !this.validateTokenInDictionary(result, 'variable', lineNum)) {
            return null;
        }
        
        // 6. Парсинг и валидация выражения
        const expr = this.parseAndValidateExpression(expression, lineNum);
        if (!expr) return null;
        
        // 7. Проверка на фатальные ошибки перед возвратом
        if (this.hasFatalErrors()) return null;
        
        return {
            nr: String(lineNum),
            conditionLeft,
            conditionOperator: conditionOp,
            conditionRight,
            expressionResult: result,
            expressionLeft: expr.left,
            expressionOperator: expr.op,
            expressionRight: expr.right,
            postProcessor: expr.postProcessor
        };
    }
    
    /**
     * Парсинг и валидация присваивания
     */
    private parseAndValidateAssignment(
        structure: LineStructure & { type: 'assignment' },
        lineNum: number
    ): ParsedLine | null {
        // Извлекаем поля с проверкой на undefined
        const result = structure.result || '';
        const expression = structure.expression || '';
        
        // 1. Валидация результата присваивания
        if (!result || !this.validateTokenInDictionary(result, 'variable', lineNum)) {
            return null;
        }
        
        // 2. Парсинг и валидация выражения
        const expr = this.parseAndValidateExpression(expression, lineNum);
        if (!expr) return null;
        
        // 3. Проверка на фатальные ошибки перед возвратом
        if (this.hasFatalErrors()) return null;
        
        return {
            nr: String(lineNum),
            conditionLeft: '',
            conditionOperator: '',
            conditionRight: '',
            expressionResult: result,
            expressionLeft: expr.left,
            expressionOperator: expr.op,
            expressionRight: expr.right,
            postProcessor: expr.postProcessor
        };
    }
    
    /**
     * Парсинг и валидация выражения
     */
    private parseAndValidateExpression(expr: string, lineNum: number): ExpressionParts | null {
        // 1. Извлечение пост-процессора
        const extractionResult = this.extractPostProcessor(expr, lineNum);
        if (!extractionResult) {
            return null;
        }
        
        const { expression: cleanExpr, postProcessor } = extractionResult;
        
        // 2. Поиск математического оператора
        const { left, op, right } = this.findMathOperator(cleanExpr);
        
        // 3. Валидация левой части
        if (!left || !this.validateTokenInDictionary(left, 'variableOrConstantOrNumber', lineNum)) {
            return null;
        }
        
        // 4. Валидация оператора (если есть)
        if (op && !this.validateTokenInDictionary(op, 'mathOperator', lineNum)) {
            return null;
        }
        
        // 5. Валидация правой части (если есть)
        if (right && !this.validateTokenInDictionary(right, 'variableOrConstantOrNumber', lineNum)) {
            return null;
        }
        
        // 6. Проверка на фатальные ошибки перед возвратом
        if (this.hasFatalErrors()) return null;
        
        return { left, op, right, postProcessor };
    }
    
    /**
     * Извлечение пост-процессора
     */
    private extractPostProcessor(
        expr: string, 
        lineNum: number
    ): { expression: string; postProcessor: string } | null {
        let postProcessor = '';
        let cleanExpr = expr;
        
        const postProcessorMatch = expr.match(/\s*>>\s*(\w+)$/);
        if (postProcessorMatch) {
            postProcessor = postProcessorMatch[1];
            // Безопасное получение индекса
            const matchIndex = postProcessorMatch.index || 0;
            cleanExpr = expr.slice(0, matchIndex).trim();
            
            // Валидация пост-процессора
            if (!postProcessor || !this.validateTokenInDictionary(postProcessor, 'postProcessor', lineNum)) {
                return null;
            }
        }
        
        return { expression: cleanExpr, postProcessor };
    }
    
    /**
     * Поиск математического оператора в выражении
     */
    private findMathOperator(expr: string): { left: string; op: string; right: string } {
        let left = expr.trim();
        let op = '';
        let right = '';
        
        if (!expr.trim()) {
            return { left: '', op: '', right: '' };
        }
        
        // Проходим по всем математическим операторам
        for (const mathOp of this.config.dictionary.mathOps) {
            // Экранируем специальные символы для регулярного выражения
            const escapedOp = mathOp.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            const pattern = new RegExp(`^(.+?)\\s*${escapedOp}\\s*(.+)$`);
            const match = expr.match(pattern);
            
            if (match) {
                left = match[1].trim();
                op = mathOp;
                right = match[2].trim();
                break;
            }
        }
        
        return { left, op, right };
    }
    
    /**
     * Валидация токена по словарю
     */
    private validateTokenInDictionary(
        token: string, 
        tokenType: 'variable' | 'variableOrConstant' | 'variableOrConstantOrNumber' | 
                   'conditionOperator' | 'mathOperator' | 'postProcessor',
        lineNum: number
    ): boolean {
        if (!token || token.trim() === '') {
            // Для некоторых типов токенов пустое значение допустимо
            if (tokenType === 'variable' || tokenType === 'conditionOperator' || 
                tokenType === 'mathOperator' || tokenType === 'postProcessor') {
                this.addError(lineNum, `Пустое значение для токена типа "${tokenType}"`, 'error');
                return false;
            }
            return true; // Для других типов пустое значение допустимо
        }
        
        const trimmedToken = token.trim();
        
        switch (tokenType) {
            case 'variable':
                return this.validateVariable(trimmedToken, lineNum);
                
            case 'variableOrConstant':
                return this.validateVariableOrConstant(trimmedToken, lineNum);
                
            case 'variableOrConstantOrNumber':
                return this.validateVariableOrConstantOrNumber(trimmedToken, lineNum);
                
            case 'conditionOperator':
                return this.validateConditionOperator(trimmedToken, lineNum);
                
            case 'mathOperator':
                return this.validateMathOperator(trimmedToken, lineNum);
                
            case 'postProcessor':
                return this.validatePostProcessor(trimmedToken, lineNum);
                
            default:
                this.addError(lineNum, `Неизвестный тип токена для валидации: ${tokenType}`, 'error');
                return false;
        }
    }
    
    /**
     * Валидация переменной
     */
    private validateVariable(token: string, lineNum: number): boolean {
        // Проверяем формат переменной
        if (!/^\w+(?:\.\w+)*$/.test(token)) {
            this.addError(lineNum, `Неверный формат переменной: "${token}"`, 'error');
            return false;
        }
        
        // Добавляем в использованные переменные
        this.usedVars.add(token);
        
        // Проверяем по словарю
        if (!this.config.dictionary.variables.includes(token)) {
            this.addError(
                lineNum, 
                `Неизвестная переменная: "${token}"`,
                this.config.validationMode === 'strict' ? 'error' : 'warning'
            );
            return this.config.validationMode !== 'strict';
        }
        
        return true;
    }
    
    /**
     * Валидация переменной или константы
     */
    private validateVariableOrConstant(token: string, lineNum: number): boolean {
        // Проверяем число
        if (/^\d+(\.\d+)?$/.test(token)) {
            return true;
        }
        
        // Проверяем константу
        if (this.config.dictionary.constants.includes(token)) {
            return true;
        }
        
        // Проверяем переменную
        return this.validateVariable(token, lineNum);
    }
    
    /**
     * Валидация переменной, константы или числа
     */
    private validateVariableOrConstantOrNumber(token: string, lineNum: number): boolean {
        // Проверяем число
        if (/^\d+(\.\d+)?$/.test(token)) {
            return true;
        }
        
        // Проверяем константу
        if (this.config.dictionary.constants.includes(token)) {
            return true;
        }
        
        // Проверяем переменную
        return this.validateVariable(token, lineNum);
    }
    
    /**
     * Валидация оператора условия
     */
    private validateConditionOperator(token: string, lineNum: number): boolean {
        if (!this.config.dictionary.compareOps.includes(token)) {
            this.addError(lineNum, `Неизвестный оператор условия: "${token}"`, 'error');
            return false;
        }
        return true;
    }
    
    /**
     * Валидация математического оператора
     */
    private validateMathOperator(token: string, lineNum: number): boolean {
        if (!this.config.dictionary.mathOps.includes(token)) {
            this.addError(lineNum, `Неизвестный математический оператор: "${token}"`, 'error');
            return false;
        }
        return true;
    }
    
    /**
     * Валидация пост-процессора
     */
    private validatePostProcessor(token: string, lineNum: number): boolean {
        if (!this.config.dictionary.postProcessors.includes(token)) {
            this.addError(lineNum, `Неизвестный пост-процессор: "${token}"`, 'error');
            return false;
        }
        return true;
    }
    
    /**
     * Проверка наличия фатальных ошибок
     */
    private hasFatalErrors(): boolean {
        return this.errors.some(error => 
            error.type === 'ERROR' || 
            (error.type === 'WARNING' && this.config.validationMode === 'strict')
        );
    }
    
    /**
     * Добавление ошибки
     */
    private addError(lineNum: number, message: string, severity: 'error' | 'warning' = 'error'): void {
        const type: 'ERROR' | 'WARNING' = severity === 'error' ? 'ERROR' : 'WARNING';
        
        const error: ErrorInfo = {
            type,
            message,
            line: lineNum
        };
        
        this.errors.push(error);
        
        // В режиме strict предупреждения тоже считаются ошибками
        if (type === 'WARNING' && this.config.validationMode === 'strict') {
            error.type = 'ERROR';
        }
    }
    
    /**
     * Генерация текста из JSON
     */
    generateScript(jsonData: { lines: ParsedLine[] }): string {
        const lines: string[] = [];
        
        for (const line of jsonData.lines) {
            const generated = this.generateLine(line);
            if (generated) {
                lines.push(generated);
            }
        }
        
        return lines.join('\n');
    }
    
    /**
     * Генерация одной строки
     */
    generateLine(line: ParsedLine): string {
        const {
            nr,
            conditionLeft,
            conditionOperator,
            conditionRight,
            expressionResult,
            expressionLeft,
            expressionOperator,
            expressionRight,
            postProcessor
        } = line;
        
        const parts: string[] = [];
        
        // Начинаем с номера строки
        parts.push(nr ? `${nr} ` : '');
        
        // Если есть условие
        if (conditionLeft && conditionOperator && conditionRight) {
            parts.push(`if ${conditionLeft} ${conditionOperator} ${conditionRight} then `);
        }
        
        // Собираем выражение
        let expression = expressionLeft;
        if (expressionOperator && expressionRight) {
            expression = `${expressionLeft} ${expressionOperator} ${expressionRight}`;
        }
        
        // Добавляем пост-процессор
        if (postProcessor) {
            expression = `${expression} >>${postProcessor}`;
        }
        
        // Добавляем присваивание
        parts.push(`${expressionResult} = ${expression}`);
        
        return parts.join('');
    }
    
    /**
     * Установка словаря переменных из калькулятора
     */
    setVariablesFromCalculator(vars: CalculatorVar[]): void {
        this.config.dictionary.variables = vars.map(v => v.varCode);
    }
    
    /**
     * Добавление переменной в словарь
     */
    addVariable(varName: string): void {
        if (!this.config.dictionary.variables.includes(varName)) {
            this.config.dictionary.variables.push(varName);
        }
    }
    
    /**
     * Получение отчета
     */
    getReport(): {
        totalLines: number;
        unknownVariables: string[];
        errors: ErrorInfo[];
        warnings: ErrorInfo[];
    } {
        const unknownVars: string[] = [];
        
        for (const varName of this.usedVars) {
            if (!this.config.dictionary.variables.includes(varName)) {
                unknownVars.push(varName);
            }
        }
        
        return {
            totalLines: this.usedVars.size,
            unknownVariables: unknownVars,
            errors: this.errors.filter(e => e.type === 'ERROR'),
            warnings: this.errors.filter(e => e.type === 'WARNING')
        };
    }
}