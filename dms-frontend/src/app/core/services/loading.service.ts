import { Injectable, signal } from '@angular/core';

/**
 * 載入狀態服務
 * 用於管理與提供全域或局部的載入中 (loading) 狀態。
 */
@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  /**
   * 內部的載入狀態信號
   */
  private readonly _isLoading = signal<boolean>(false);

  /**
   * 對外提供的唯讀載入狀態，供樣板或元件訂閱與讀取
   */
  public readonly isLoading = this._isLoading.asReadonly();

  /**
   * 顯示載入中狀態 (設定 isLoading 為 true)
   */
  show(): void {
    this._isLoading.set(true);
  }

  /**
   * 隱藏載入中狀態 (設定 isLoading 為 false)
   */
  hide(): void {
    this._isLoading.set(false);
  }
}
