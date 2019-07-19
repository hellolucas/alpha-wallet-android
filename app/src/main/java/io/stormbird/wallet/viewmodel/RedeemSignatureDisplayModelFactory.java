package io.stormbird.wallet.viewmodel;

/**
 * Created by James on 25/01/2018.
 */

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import io.stormbird.wallet.interact.*;
import io.stormbird.wallet.interact.GenericWalletInteract;
import io.stormbird.wallet.router.AssetDisplayRouter;
import io.stormbird.wallet.service.AssetDefinitionService;

/**
 * Created by James on 22/01/2018.
 */

public class RedeemSignatureDisplayModelFactory implements ViewModelProvider.Factory {

    private final GenericWalletInteract genericWalletInteract;
    private final FindDefaultNetworkInteract findDefaultNetworkInteract;
    private final SignatureGenerateInteract signatureGenerateInteract;
    private final CreateTransactionInteract createTransactionInteract;
    private final FetchTokensInteract fetchTokensInteract;
    private final MemPoolInteract memPoolInteract;
    private final AssetDisplayRouter assetDisplayRouter;
    private final AssetDefinitionService assetDefinitionService;

    public RedeemSignatureDisplayModelFactory(
            GenericWalletInteract genericWalletInteract,
            SignatureGenerateInteract signatureGenerateInteract,
            CreateTransactionInteract createTransactionInteract,
            FindDefaultNetworkInteract findDefaultNetworkInteract,
            FetchTokensInteract fetchTokensInteract,
            MemPoolInteract memPoolInteract,
            AssetDisplayRouter assetDisplayRouter,
            AssetDefinitionService assetDefinitionService) {
        this.genericWalletInteract = genericWalletInteract;
        this.findDefaultNetworkInteract = findDefaultNetworkInteract;
        this.signatureGenerateInteract = signatureGenerateInteract;
        this.createTransactionInteract = createTransactionInteract;
        this.fetchTokensInteract = fetchTokensInteract;
        this.memPoolInteract = memPoolInteract;
        this.assetDisplayRouter = assetDisplayRouter;
        this.assetDefinitionService = assetDefinitionService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RedeemSignatureDisplayModel(genericWalletInteract, signatureGenerateInteract, createTransactionInteract, findDefaultNetworkInteract, fetchTokensInteract, memPoolInteract, assetDisplayRouter, assetDefinitionService);
    }
}
