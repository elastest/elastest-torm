import { Component, OnInit } from '@angular/core';
import { ElasticsearchApiService } from './elasticsearch-api.service';

@Component({
  selector: 'etm-manage-elasticsearch',
  templateUrl: './manage-elasticsearch.component.html',
  styleUrls: ['./manage-elasticsearch.component.scss'],
})
export class ManageElasticsearchComponent implements OnInit {
  redIndices: string[];
  loadingRedIndices: boolean = false;

  redIndicesLabel: string =
    'Elasticsearch indices can be in three different states: green, yellow and red. If any index is in red, Elasticsearch' +
    ' will have problems and will not be able to create new indexes to save logs and metrics of the executions.';

  redIndicesSublabel: string =
    'The easiest solution is to delete all indexes in red state, although the traces stored in them will be lost. You can check if there' +
    ' is any red index by clicking on the "Load Red Indices" button. Then you can delete them all with the button "Delete red indexes"';

  allIndices: string[];
  loadingAllIndices: boolean = false;

  constructor(private elasticsearchApiService: ElasticsearchApiService) {}

  ngOnInit(): void {}

  loadRedIndices(): void {
    this.loadingRedIndices = true;
    this.elasticsearchApiService.getRedIndices().subscribe(
      (redIndices: string[]) => {
        this.redIndices = redIndices ? redIndices : [];
        this.loadingRedIndices = false;
      },
      (error: Error) => {
        console.log(error);
        this.loadingRedIndices = false;
      },
    );
  }

  deleteRedIndices(): void {
    this.elasticsearchApiService.deleteRedIndices().subscribe(
      (deleted: boolean) => {
        this.redIndices = [];
      },
      (error: Error) => console.log(error),
    );
  }

  getAllIndices(): void {
    this.loadingAllIndices = true;
    this.elasticsearchApiService.getAllIndices().subscribe(
      (indices: string[]) => {
        this.allIndices = indices ? indices : [];
        this.loadingAllIndices = false;
      },
      (error: Error) => {
        console.log(error);
        this.loadingAllIndices = false;
      },
    );
  }
}
