export class TestEngineModel {
  name: string;
  url: string;
  imagesList: string[];
  statusMsg: string;
  status: '' | 'Not initialized' | 'Initializing' | 'Pulling' | 'Starting' | 'Ready';

  constructor() {
    this.name = '';
    this.url = '';
    this.imagesList = [];
    this.statusMsg = '';
    this.status = '';
  }

  isNotInitialized(): boolean {
    return this.status === 'Not initialized';
  }

  isStarting(): boolean {
    return this.status === 'Starting';
  }

  isReady(): boolean {
    return this.status === 'Ready';
  }

  isCreated(): boolean {
    return !this.isNotInitialized();
  }
}
