export class BoxDevice {
  deviceName: string = '';
  deviceTypeCode: string = '';
  tradeMark: string = '';
  model: string = '';
  serialNr: string = '';
  licenseKey: string = '';
  imei: string = '';
  osName: string = '';
  osVersion: string = '';
  countryCode: string = '';
  devicePrice: string = '';

  constructor(data?: Partial<BoxDevice>) {
    if (data) {
      Object.assign(this, data);
    }
  }
}
