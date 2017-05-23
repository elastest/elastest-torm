export class TestResult {
  public tests: string;
  public errors: string;
  public failures: string;
  public skipped: string;

  constructor(tests: string, errors: string, failures: string, skipped: string){
    this.tests = tests;
    this.errors = errors;
    this.failures = failures;
    this.skipped = skipped;
  }
}
