export class CompleteUrlObj extends URL {
    queryParams: any;
}

export function getUrlQueryParams(url: string): any {
    let hash: string[];
    let myJson: any = {};
    let hashes: string[] = url.slice(url.indexOf('?') + 1).split('&');
    for (let i: number = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        myJson[hash[0]] = hash[1];
    }
    return myJson;
}

export function getUrlObj(url: string): CompleteUrlObj {
    let urlObj: any = new URL(url);
    urlObj.queryParams = getUrlQueryParams(url);
    return urlObj;
}
