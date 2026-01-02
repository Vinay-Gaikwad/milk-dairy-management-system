package com.milk.milkrun.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.milk.milkrun.collectiondatabase.CollectionDatabase;
import com.milk.milkrun.collectiondatabase.MilkCollection;
import com.milk.milkrun.collectiondatabase.MilkCollectionDao;
import com.milk.milkrun.customerdatabase.Customer;
import com.milk.milkrun.customerdatabase.CustomerDatabase;
import com.milk.milkrun.customerdatabase.CustomerDao;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final MilkCollectionDao milkDao;
    private final CustomerDao customerDao;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        milkDao = CollectionDatabase.getInstance(application).milkCollectionDao();
        customerDao = CustomerDatabase.getInstance(application).customerDao();
    }

    public LiveData<List<MilkCollection>> getTodayMilkSummary(String date) {
        return milkDao.getCollectionsForDateLive(date);
    }

    public LiveData<List<MilkCollection>> getRecentEntries() {
        return milkDao.getRecentEntriesLive();
    }

    public Customer getCustomerByNumber(String number) {
        return customerDao.getCustomerByNumber(number);
    }
}
