create service and method validator_to_text
input value is array of json like 
[
    {
        "keyRight": "pl_endDate",
        "keyLeft": "pl_startDate",
        "dataType": "NUMBER",
        "ruleType": "=",
        "errorText": "fsadfasdfasdf sdfasadfasdfasd saffasdfasdfa",
        "valueRight": "4"
    }
],

keyRight and keyLeft is a code from product.vars
AudioScheduledSourceNode;ld be replaced with name
is keyRight is null, then use valueRight

if errorText = "AND", it means that this is multi line rule

example text for - "Var1 must be not empty, if not show error {}"
"var1 must be > than 5, if not show error {}"

"IF var1 not null AND" if errortext = "AND"
