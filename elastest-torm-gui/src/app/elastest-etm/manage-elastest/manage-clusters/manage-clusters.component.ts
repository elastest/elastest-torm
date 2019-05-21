import { Component, OnInit } from '@angular/core';
import { ManageClustersService } from './manage-clusters.service';
import { PopupService } from '../../../shared/services/popup.service';
import { ITdDataTableColumn, ITdDataTableRowClickEvent } from '@covalent/core';

@Component({
  selector: 'etm-manage-clusters',
  templateUrl: './manage-clusters.component.html',
  styleUrls: ['./manage-clusters.component.scss'],
})
export class ManageClustersComponent implements OnInit {
  clusters: any[] = [];
  loadingClusters: boolean = false;
  // Cluster  data
  clusterColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'type', label: 'Type' },
    { name: 'master', label: 'Master' },
    { name: 'nodes', label: 'Nodes' },
    { name: 'resourceGroupId', label: 'Resource Group ID' },
    { name: 'options', label: 'Options', sortable: false },
  ];

  selectedCluster: any;

  nodes: any[] = [];
  nodesColumns: ITdDataTableColumn[] = [
    { name: 'id', label: 'Id', width: 80 },
    { name: 'ip', label: 'Ip' },
    { name: 'vduId', label: 'Vdu Id' },
    { name: 'epmIp', label: 'Epm Ip' },
    { name: 'type', label: 'Type' },
    { name: 'authCredentials', label: 'Auth Credentials' },
    { name: 'options', label: 'Options', sortable: false },
  ];

  constructor(private manageClustersService: ManageClustersService, private popupService: PopupService) {}

  ngOnInit(): void {}

  onUploadClusterFile(file: File): void {
    if (file instanceof File) {
      this.manageClustersService.uploadClusterTarFile(file).subscribe(
        (response: any) => {
          this.popupService.openSnackBar('The file has been uploaded succesfully');
        },
        (error: Error) => {
          console.log(error);
          this.popupService.openSnackBar('An error has occurred in uploading file');
        },
      );
    } else {
      this.popupService.openSnackBar('File not allowed');
    }
  }

  onUploadNodeFile(file: File): void {
    if (file instanceof File) {
      this.manageClustersService.uploadNodeTarFile(file).subscribe(
        (response: any) => {
          this.popupService.openSnackBar('The file has been uploaded succesfully');
        },
        (error: Error) => {
          console.log(error);
          this.popupService.openSnackBar('An error has occurred in uploading file');
        },
      );
    } else {
      this.popupService.openSnackBar('File not allowed');
    }
  }

  loadAllClusters(): void {
    this.loadingClusters = true;
    this.manageClustersService.getAllClusters().subscribe(
      (clusters: any[]) => {
        this.clusters = clusters;
        console.log(clusters);
        this.loadingClusters = false;
      },
      (error: Error) => {
        console.log(error);
        this.popupService.openSnackBar('An error has occurred on get clusters');
        this.loadingClusters = false;
      },
    );
  }

  loadAllNodes(clusterId: string): void {
    this.manageClustersService.getAllNodes(clusterId).subscribe(
      (nodes: any[]) => {
        this.nodes = nodes;
        console.log(nodes);
      },
      (error: Error) => console.log(error),
    );
  }

  selectCluster(event: ITdDataTableRowClickEvent): void {
    this.selectedCluster = event.row;
    this.nodes = this.selectedCluster.nodes ? this.selectedCluster.nodes : [];
  }
}
