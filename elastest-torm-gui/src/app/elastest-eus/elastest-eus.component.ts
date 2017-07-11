import { Component, OnInit, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'app-elastest-eus',
  templateUrl: './elastest-eus.component.html',
  styleUrls: ['./elastest-eus.component.scss']
})
export class ElastestEusComponent implements OnInit {

  componentTitle: string = "Project Management";

  @Output()
  onInitComponent = new EventEmitter<string>();

  constructor() { }

  ngOnInit() {
  }

}
