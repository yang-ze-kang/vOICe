package com.example.lbstest.util.rx;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class OnlyNextObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {

    }
}
