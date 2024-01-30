@echo Building pattern library and all sample systems...
@echo To run individual examples, type "testPattern [patternName]",
@echo where [patternName] is "observer", "chainOfResponsibility", etc.

ajc -d bin @src/allPatterns.lst


