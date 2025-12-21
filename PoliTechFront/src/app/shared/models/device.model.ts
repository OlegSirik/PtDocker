export class Device {
  deviceName: string = '';
  deviceTypeCode?: string;
  tradeMark?: string;
  model?: string ;
  serialNr?: string;
  licenseKey?: string;
  imei?: string;
  osName?: string;
  osVersion?: string;
  countryCode?: string;
  devicePrice?: number = 0;

  constructor(data?: Partial<Device>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}