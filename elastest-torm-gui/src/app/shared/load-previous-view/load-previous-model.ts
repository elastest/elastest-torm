export interface LoadPreviousModel {
    prevTraces: string[];
    prevLoaded: boolean;
    hidePrevBtn: boolean;
    startDate: Date;
    endDate: Date;
    
    loadPrevious();
}
