export class DockerServiceStatus {
  statusMsg: string;
  status: '' | 'Not initialized' | 'Initializing' | 'Pulling' | 'Starting' | 'Ready';

  constructor() {
    this.status = '';
    this.statusMsg = '';
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
