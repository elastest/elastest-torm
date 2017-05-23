import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { TestInfo } from './test-info';
import { TestResult } from './test-result';
import 'rxjs/Rx';
import { Observable } from 'rxjs/Observable';


@Injectable()
export class TestManagerService {

  constructor(private http: Http) { }

  createAndRunTest(testInfo: TestInfo) {
    //let url = 'http://localhost:8080/containers/';
    let url = 'http://localhost:8090/containers/external/api/';
    return this.http.post(url, testInfo)
      .map(response => this.createTestInfo(response.json()))
  }

  getTestResults() {
    console.log("Invoking api rest to get the test results");
    let url = 'http://localhost:8090/containers/testInfo';
    return this.http.get(url)
      .map(
      response => this.createTestInfo(response.json()),
      error => console.log(error)
      )
  }

  createTest(testInfo: any) {
    return 1;
  }

  runTest(testInfo: any) {
    return 1;
  }

  createTestInfo(testInfo: any[]) {
    console.log("Test info retrives:" + testInfo[0].numberOfErrors);
    var testResult = new TestResult(testInfo[0].numberOfTests, testInfo[0].numberOfErrors, testInfo[0].numberOfFailures, testInfo[0].numberOfSkipped);
    return testResult;
  }

  checkUrlStatus(url: string) {
    return this.http
      .get(url)
      .map((res) => {
        if (res) {
          console.log("response status: " + res);
          if (res.status >= 200 && res.status < 300) {
            return [{ status: res.status, json: res }]
          }
        }
      }).catch((error: any) => {
        console.log("Error status code: " + error);
        if (error.status < 400 || error.status === 500) {
          return Observable.throw(new Error(error.status));
        }
      })
  }
}
