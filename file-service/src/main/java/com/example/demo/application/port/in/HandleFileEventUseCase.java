package com.example.demo.application.port.in;

import com.example.demo.application.shared.command.FileBoundCommand;
import com.example.demo.application.shared.command.FileDeletedCommand;

/**
 * 處理檔案事件的 Inbound Port (UseCase)。
 * <p>
 * 定義了應用層處理檔案綁定與檔案刪除事件的業務功能介面。
 * 接收 Command 並執行對應的業務邏輯。
 * </p>
 */
public interface HandleFileEventUseCase {

    /**
     * 處理檔案綁定事件。
     *
     * @param command 檔案綁定命令
     * @throws Exception 處理過程中可能發生的例外
     */
    void handleFileBoundEvent(FileBoundCommand command) throws Exception;

    /**
     * 處理檔案刪除事件。
     *
     * @param command 檔案刪除命令
     * @throws Exception 處理過程中可能發生的例外
     */
    void handleFileDeletedEvent(FileDeletedCommand command) throws Exception;
}
