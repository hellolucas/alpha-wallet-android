package io.stormbird.wallet.di;


import dagger.Module;
import dagger.Provides;
import io.stormbird.wallet.interact.CreateTransactionInteract;
import io.stormbird.wallet.interact.FindDefaultNetworkInteract;
import io.stormbird.wallet.interact.GenericWalletInteract;
import io.stormbird.wallet.repository.EthereumNetworkRepositoryType;
import io.stormbird.wallet.repository.PasswordStore;
import io.stormbird.wallet.repository.TransactionRepositoryType;
import io.stormbird.wallet.repository.WalletRepositoryType;
import io.stormbird.wallet.router.GasSettingsRouter;
import io.stormbird.wallet.service.GasService;
import io.stormbird.wallet.service.TokensService;
import io.stormbird.wallet.viewmodel.ConfirmationViewModelFactory;

@Module
public class ConfirmationModule {
    @Provides
    public ConfirmationViewModelFactory provideConfirmationViewModelFactory(
            GenericWalletInteract genericWalletInteract,
            GasService gasService,
            CreateTransactionInteract createTransactionInteract,
            GasSettingsRouter gasSettingsRouter,
            TokensService tokensService,
            FindDefaultNetworkInteract findDefaultNetworkInteract
    ) {
        return new ConfirmationViewModelFactory(genericWalletInteract, gasService, createTransactionInteract, gasSettingsRouter, tokensService, findDefaultNetworkInteract);
    }

    @Provides
    GenericWalletInteract provideFindDefaultWalletInteract(WalletRepositoryType walletRepository) {
        return new GenericWalletInteract(walletRepository);
    }

    @Provides
    CreateTransactionInteract provideCreateTransactionInteract(TransactionRepositoryType transactionRepository, PasswordStore passwordStore) {
        return new CreateTransactionInteract(transactionRepository, passwordStore);
    }

    @Provides
    GasSettingsRouter provideGasSettingsRouter() {
        return new GasSettingsRouter();
    }

    @Provides
    FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
            EthereumNetworkRepositoryType networkRepository) {
        return new FindDefaultNetworkInteract(networkRepository);
    }
}
