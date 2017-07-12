import { ElastestESService } from '../../services/elastest-es.service';
import { ESLogModel } from './elasticsearch-log-model';

export class RabESLogModel extends ESLogModel {

    constructor(elastestESService: ElastestESService) {
        super(elastestESService);
    }
}
