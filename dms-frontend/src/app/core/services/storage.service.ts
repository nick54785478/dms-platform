import { Injectable } from '@angular/core';

/**
 * 網頁儲存服務
 * 提供針對瀏覽器 localStorage 與 sessionStorage 的安全操作封裝，支援伺服器端渲染 (SSR) 的環境判斷。
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {
  
  /**
   * 檢查當前執行環境是否為瀏覽器
   * @returns 若在瀏覽器環境則回傳 true，否則回傳 false
   */
  private isBrowser(): boolean {
    return typeof window !== 'undefined';
  }

  /**
   * 將鍵值對儲存至指定的 Storage 中
   * @param key 儲存的鍵名
   * @param value 儲存的值 (字串格式)
   * @param storageType 儲存類型，可選 'local' (localStorage) 或 'session' (sessionStorage)，預設為 'local'
   */
  setItem(key: string, value: string, storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.setItem(key, value);
    }
  }

  /**
   * 從指定的 Storage 中取得對應鍵名的值
   * @param key 要查詢的鍵名
   * @param storageType 儲存類型，可選 'local'或 'session'，預設為 'local'
   * @returns 回傳對應的值，若不存在或不在瀏覽器環境中則回傳 null
   */
  getItem(key: string, storageType: 'local' | 'session' = 'local'): string | null {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      return storage.getItem(key);
    }
    return null;
  }

  /**
   * 從指定的 Storage 中移除特定鍵名的資料
   * @param key 要移除的鍵名
   * @param storageType 儲存類型，可選 'local' 或 'session'，預設為 'local'
   */
  removeItem(key: string, storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.removeItem(key);
    }
  }

  /**
   * 清空指定 Storage 的所有資料
   * @param storageType 儲存類型，可選 'local' 或 'session'，預設為 'local'
   */
  clear(storageType: 'local' | 'session' = 'local'): void {
    if (this.isBrowser()) {
      const storage = storageType === 'local' ? window.localStorage : window.sessionStorage;
      storage.clear();
    }
  }
}
