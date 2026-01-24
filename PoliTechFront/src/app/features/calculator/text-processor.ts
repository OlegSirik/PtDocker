import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { FormulaLine, CalculatorVar } from '../../shared/services/calculator.service';
import { SimpleScriptProcessor } from './simpleTextProcessor';
import { ParseErrorsDialogComponent } from './parse-errors-dialog/parse-errors-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class TextProcessorService {
  private dialog = inject(MatDialog);

  /**
   * Converts formula lines to text format
   * @param lines Array of formula lines to convert
   * @param vars Array of calculator variables for dictionary
   * @returns Text representation of the lines (one line per formula line)
   */
  toText(lines: FormulaLine[], vars: CalculatorVar[] = []): string {
    if (!lines || lines.length === 0) {
      return '';
    }

    // Create processor with variables from calculator
    const processor = new SimpleScriptProcessor({
      variables: vars.map(v => v.varCode)
    });

    // Sort lines by nr (as number)
    const sortedLines = [...lines].sort((a, b) => {
      const aNr = parseInt(a.nr || '0', 10);
      const bNr = parseInt(b.nr || '0', 10);
      return (isNaN(aNr) ? 0 : aNr) - (isNaN(bNr) ? 0 : bNr);
    });

    // Convert to format expected by SimpleScriptProcessor
    const parsedLines = sortedLines.map(line => ({
      nr: line.nr || '',
      conditionLeft: line.conditionLeft || '',
      conditionOperator: line.conditionOperator || '',
      conditionRight: line.conditionRight || '',
      expressionResult: line.expressionResult || '',
      expressionLeft: line.expressionLeft || '',
      expressionOperator: line.expressionOperator || '',
      expressionRight: line.expressionRight || '',
      postProcessor: line.postProcessor || ''
    }));

    return processor.generateScript({ lines: parsedLines });
  }

  /**
   * Converts text format to formula lines
   * @param text Text representation of formula lines
   * @param vars Array of calculator variables for dictionary
   * @param conditionOperatorOptions Array of valid condition operators
   * @param expressionOperatorOptions Array of valid expression operators
   * @param postProcessorOptions Array of valid post processors
   * @returns Array of formula lines, or null if parsing produced errors (dialog shown, process stopped)
   */
  fromText(
    text: string, 
    vars: CalculatorVar[] = [],
    conditionOperatorOptions: string[] = [],
    expressionOperatorOptions: string[] = [],
    postProcessorOptions: string[] = []
  ): FormulaLine[] | null {
    if (!text || text.trim() === '') {
      return [];
    }

    // Create processor with variables and options from calculator
    const processor = new SimpleScriptProcessor({
      variables: vars.map(v => v.varCode),
      compareOps: conditionOperatorOptions,
      mathOps: expressionOperatorOptions,
      postProcessors: postProcessorOptions,
      validationMode: 'warn' // Use warn mode to allow unknown variables
    });

    const result = processor.parseScript(text);

    if (result.errors.length > 0) {
      const message = result.errors
        .map(e => `Строка ${e.line}: ${e.message}`)
        .join('\n');
      this.dialog.open(ParseErrorsDialogComponent, {
        width: '500px',
        data: { message }
      });
      return null;
    }

    // Convert parsed lines to FormulaLine format
    return result.lines.map(line => ({
      nr: line.nr || '1',
      conditionLeft: line.conditionLeft || '',
      conditionOperator: line.conditionOperator || '',
      conditionRight: line.conditionRight || '',
      expressionResult: line.expressionResult || '',
      expressionLeft: line.expressionLeft || '',
      expressionOperator: line.expressionOperator || '',
      expressionRight: line.expressionRight || '',
      postProcessor: line.postProcessor || ''
    }));
  }
}
