package io.stormbird.wallet.entity;

/**
 * Created by James on 18/07/2019.
 * Stormbird in Sydney
 */
public interface BackupTokenCallback
{
    void BackupClick(String address);
    void remindMeLater(String address);
}
