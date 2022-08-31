# statgen

## Code

### Requirements
- Stanford NLP's CorNLP
- Stanford Phrasal
- Microsoft Z3

### Tasks
- Generate parallel corpus of documentations and implementations: `class eval.ParallelCorpusParser`.
- Generate test parallel corpus for suggesting documentation: `class eval.GenerateParallelTestForSuggestion`.
- Generate test parallel corpus for detecting inconsistency: `class eval.GenerateParallelTestForDetection`.
- Translate: `class translation.Translator`
