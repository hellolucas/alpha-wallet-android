package io.stormbird.wallet.viewmodel;

import android.app.DownloadManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.*;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.stormbird.token.entity.MagicLinkData;
import io.stormbird.token.tools.ParseMagicLink;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.*;
import io.stormbird.wallet.interact.FetchWalletsInteract;
import io.stormbird.wallet.interact.GenericWalletInteract;
import io.stormbird.wallet.repository.LocaleRepositoryType;
import io.stormbird.wallet.repository.PreferenceRepositoryType;
import io.stormbird.wallet.router.AddTokenRouter;
import io.stormbird.wallet.router.ExternalBrowserRouter;
import io.stormbird.wallet.router.ImportTokenRouter;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.HDKeyService;
import io.stormbird.wallet.ui.HomeActivity;
import io.stormbird.wallet.util.LocaleUtils;

import java.io.File;
import java.io.FilenameFilter;

import static org.web3j.crypto.WalletUtils.isValidAddress;

public class HomeViewModel extends BaseViewModel {
    private final String TAG = "HVM";
    public static final String ALPHAWALLET_DIR = "AlphaWallet";
    public static final String ALPHAWALLET_FILE_URL = "https://1x.alphawallet.com/dl/latest.apk";
    private static final int TIMER_FREQUENCY = 1000;

    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Transaction[]> transactions = new MutableLiveData<>();
    private final MutableLiveData<Wallet[]> wallets = new MutableLiveData<>();

    private final PreferenceRepositoryType preferenceRepository;
    private final ExternalBrowserRouter externalBrowserRouter;
    private final ImportTokenRouter importTokenRouter;
    private final AddTokenRouter addTokenRouter;
    private final LocaleRepositoryType localeRepository;
    private final AssetDefinitionService assetDefinitionService;
    private final GenericWalletInteract genericWalletInteract;
    private final FetchWalletsInteract fetchWalletsInteract;

    private CryptoFunctions cryptoFunctions;
    private ParseMagicLink parser;

    private final MutableLiveData<File> installIntent = new MutableLiveData<>();
    private final MutableLiveData<String> walletName = new MutableLiveData<>();

    HomeViewModel(
            PreferenceRepositoryType preferenceRepository,
            LocaleRepositoryType localeRepository,
            ImportTokenRouter importTokenRouter,
            ExternalBrowserRouter externalBrowserRouter,
            AddTokenRouter addTokenRouter,
            AssetDefinitionService assetDefinitionService,
            GenericWalletInteract genericWalletInteract,
            FetchWalletsInteract fetchWalletsInteract) {
        this.preferenceRepository = preferenceRepository;
        this.externalBrowserRouter = externalBrowserRouter;
        this.importTokenRouter = importTokenRouter;
        this.addTokenRouter = addTokenRouter;
        this.localeRepository = localeRepository;
        this.assetDefinitionService = assetDefinitionService;
        this.genericWalletInteract = genericWalletInteract;
        this.fetchWalletsInteract = fetchWalletsInteract;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<NetworkInfo> defaultNetwork() {
        return defaultNetwork;
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }

    public LiveData<Transaction[]> transactions() {
        return transactions;
    }

    public LiveData<File> installIntent() {
        return installIntent;
    }

    public void prepare() {
        progress.postValue(false);
    }

    public void onClean()
    {

    }

    public void showImportLink(Context context, String importData) {
        disposable = genericWalletInteract
                .find().toObservable()
                .filter(wallet -> checkWalletNotEqual(wallet, importData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wallet -> importLink(wallet, context, importData), this::onError);
    }

    private boolean checkWalletNotEqual(Wallet wallet, String importData) {
        boolean filterPass = false;

        try {
            if (cryptoFunctions == null) {
                cryptoFunctions = new CryptoFunctions();
            }
            if (parser == null) {
                parser = new ParseMagicLink(cryptoFunctions);
            }

            MagicLinkData data = parser.parseUniversalLink(importData);
            String linkAddress = parser.getOwnerKey(data);

            if (isValidAddress(data.contractAddress)) {
                filterPass = !wallet.address.equals(linkAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filterPass;
    }

    private void importLink(Wallet wallet, Context context, String importData) {
        //valid link, remove from clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clipData = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clipData);
        }
        importTokenRouter.open(context, importData);
    }

    public void openDeposit(Context context, Uri uri) {
        externalBrowserRouter.open(context, uri);
    }

    public void showAddToken(Context context, String address) {
        addTokenRouter.open(context, address);
    }

    public void setLocale(HomeActivity activity) {
        //get the current locale
        String currentLocale = localeRepository.getDefaultLocale();
        LocaleUtils.setLocale(activity, currentLocale);
    }

    public void loadExternalXMLContracts() {
        assetDefinitionService.checkExternalDirectoryAndLoad();
    }

    public void downloadAndInstall(String build, Context ctx) {
        createDirectory();
        downloadAPK(build, ctx);
    }

    private void createDirectory() {
        //create XML repository directory
        File directory = new File(
                Environment.getExternalStorageDirectory()
                        + File.separator + ALPHAWALLET_DIR);

        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private void downloadAPK(String version, Context ctx) {
        String destination = Environment.getExternalStorageDirectory()
                + File.separator + ALPHAWALLET_DIR;

        File testFile = new File(destination, "AlphaWallet-" + version + ".apk");
        if (testFile.exists()) {
            testFile.delete();
        }
        final Uri uri = Uri.parse("file://" + testFile.getPath());

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ALPHAWALLET_FILE_URL));
        request.setDescription(ctx.getString(R.string.alphawallet_update) + " " + version);
        request.setTitle(ctx.getString(R.string.app_name));
        request.setDestinationUri(uri);
        final DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                installIntent.postValue(testFile);
                ctx.unregisterReceiver(this);
            }
        };

        ctx.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void getWalletName() {
        disposable = fetchWalletsInteract
                .getWalletName(preferenceRepository.getCurrentWalletAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onWalletName, this::onError);
    }

    private void onWalletName(String name) {
        walletName.postValue(name);
    }

    public LiveData<String> walletName() {
        return walletName;
    }

    public void cleanDatabases(Context ctx)
    {
        File[] files = ctx.getFilesDir().listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File file, String name)
            {
                return name.matches("^0x\\S{40}-.+-db.real\\S+")
                        && !name.matches("^0x\\S{40}-721-db.real\\S+"); //match all deprecated databases
            }
        });

        for (File file : files)
        {
            //erase file
            deleteRecursive(file);
        }
    }

    private void deleteRecursive(File fileDir)
    {
        if (fileDir.isDirectory()) {
            for (File child : fileDir.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileDir.delete();
    }

    public void cleanNewDatabases(Context ctx)
    {
        File[] files = ctx.getFilesDir().listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File file, String name)
            {
                return name.matches("^0x\\S{40}-db.real\\S+")
                    || name.matches("^0x\\S{40}-721-db.real\\S+"); //match all deprecated databases
            }
        });

        for (File file : files)
        {
            //erase file
            deleteRecursive(file);
        }
    }

    public boolean isBackupWalletDialogShown() {
        return preferenceRepository.isBackupWalletDialogShown();
    }

    public void setBackupWalletDialogShown(boolean isShown) {
        preferenceRepository.setBackupWalletDialogShown(isShown);
    }

    public boolean isFindWalletAddressDialogShown() {
        return preferenceRepository.isFindWalletAddressDialogShown();
    }

    public void setFindWalletAddressDialogShown(boolean isShown) {
        preferenceRepository.setFindWalletAddressDialogShown(isShown);
    }

    public void upgradeWallet(String keyAddress)
    {
        genericWalletInteract.getWallet(keyAddress)
                .map(this::upgradeWallet)
                .flatMap(fetchWalletsInteract::storeWallet)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onWalletUpgraded, this::onError)
                .isDisposed();
    }

    private void onWalletUpgraded(Wallet wallet)
    {
        Log.d("HVM", "Wallet " + wallet.address + " Upgraded to: " + wallet.authLevel.toString());
    }

    private Wallet upgradeWallet(Wallet wallet)
    {
        switch (wallet.authLevel)
        {
            default:
                break;
            case NOT_SET:
                if (wallet.type == WalletType.HDKEY) wallet.authLevel = HDKeyService.AuthenticationLevel.TEE_AUTHENTICATION;
                break;
            case TEE_NO_AUTHENTICATION:
                wallet.authLevel = HDKeyService.AuthenticationLevel.TEE_AUTHENTICATION;
                break;
            case STRONGBOX_NO_AUTHENTICATION:
                wallet.authLevel = HDKeyService.AuthenticationLevel.STRONGBOX_AUTHENTICATION;
                break;
        }

        return wallet;
    }
}
