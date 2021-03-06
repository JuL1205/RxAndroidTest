package com.funtory.rxAndroidTest.viewmodel;

import android.util.Log;
import android.widget.CheckBox;

import com.funtory.rxAndroidTest.log.Logg;
import com.funtory.rxAndroidTest.model.UserFavoriteModel;
import com.funtory.rxAndroidTest.model.UserInfoModel;
import com.jakewharton.rxbinding.widget.RxCompoundButton;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by JuL on 2016. 9. 19..
 */

public class RxViewModel {

    /* creator */
    public Observable<List<Integer>> just(){
        /*
         * from 과는 다르게, collector 자체가 한번 발행된다.
         * onComplete가 자동 호출 된다.
         */
        return Observable.just(Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
    }

    public Observable<Integer> from(){
        /*
         * just 와는 다르게, collector 안의 값 하나하나가 각각 발행된다.
         * onComplete가 자동 호출 된다.
         */
        return Observable.from(Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
    }

    public Observable<String> zip() {
        /*
         * 시나리오
         * 1. 유저 기본 정보를 얻어오는 작업
         * 2. 유저 취미 정보를 얻어오는 작업
         * 3. 1, 2 작업이 완료 후 취합하여 ui에 뿌려주는 작업.
         *
         * 각 thread에서 동작하는 옵저버블 생성 후 zip 오퍼레이션을 사용해 후처리를 해본다.
         */
        Observable<UserInfoModel> getUserInfoObs = Observable.create((Observable.OnSubscribe<UserInfoModel>) subscriber -> {
            Logg.i("getUserInfoObs call");

            for(int i = 0 ; i < 10000000 ; i++){ //네트워크 시간 지연 모사

            }

            UserInfoModel model = new UserInfoModel();
            model.name = "JuL";
            model.no = 1205;
            subscriber.onNext(model);
        }).subscribeOn(Schedulers.io());


        Observable<UserFavoriteModel> getUserFavoriteObs = Observable.create((Observable.OnSubscribe<UserFavoriteModel>) subscriber -> {
            Logg.i("getUserFavoriteObs call");

            UserFavoriteModel model = new UserFavoriteModel();
            model.title = "실리콘벨리";
            model.category = "미드";
            subscriber.onNext(model);
        }).subscribeOn(Schedulers.io());


        return Observable.zip(getUserInfoObs, getUserFavoriteObs, (userInfoModel, userFavoriteModel) -> {
            Logg.i("zip call =======================");
            Logg.i("userInfoModel = " + userInfoModel);
            Logg.i("userFavoriteModel = " + userFavoriteModel);
            return "userInfoModel = " + userInfoModel + "\n" + "userFavoriteModel = " + userFavoriteModel;
        }).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> combineLatest(CheckBox cb1, CheckBox cb2) {
        /*
         * 2개의 observable에서 데이터가 발급될 때마다 이벤트가 들어온다.
         * 이 예제에서는, 각 체크박스의 상태가 변경 될때마다 이벤트가 발생되고, 두 체크 상태를 & 한 값이 결과값이 된다.
         */
        Observable<Boolean> check1 = RxCompoundButton.checkedChanges(cb1);
        Observable<Boolean> check2 = RxCompoundButton.checkedChanges(cb2);

        return Observable.combineLatest(check1, check2, (bool1, bool2) -> "Check1 : "+bool1+"\nCheck2 : "+bool2+"\nResult : "+ (bool1 & bool2));
    }

    public Observable<Integer> range(){
        /*
         * 지정된 횟수 만큼 integer 값이 발행된다. for문 대용으로 사용해도 될 듯 하다.
         * 이 예제에서는, 0부터 9까지 총 10번의 이벤트가 발행됨.
         */
        return Observable.range(0, 10);
    }

    public Observable<Long> interval(){
        /*
         * 지정된 간격으로 계속 해서 이벤트가 발생된다.
         * 기본적으로 main thread 가 아닌, computation thread 에서 동작하는 것에 주의하자.
         */
        return Observable.interval(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Long> timer(){
        /*
         * 지정된 시간 후에 한번 이벤트가 발생된다.
         * 기본적으로 main thread 가 아닌, computation thread 에서 동작하는 것에 주의하자.
         */
        return Observable.timer(1000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread());
    }







    /* operator */
    public Observable<Integer> map(){
        /*
         * 관찰한 observable 의 data를 변형한다.
         * flatMap 과는 다르게, 어떤 object 로도 변형할 수 있고, 변형된 형태 그대로 subscriber에게 발행된다.
         */
        return from().map(integer -> (integer * integer)).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> flatMap(){
        /*
         * 관찰한 observable 의 data를 변형한다.
         * map 과는 다르게, observable 로만 변형이 가능하고, 변형된 observable 안의 값이 subscriber에게 발행된다.
         * concatMap 과는 다르게, 순서보장이 안된다.
         */
        return from().flatMap(integer -> Observable.just(integer * integer).subscribeOn(Schedulers.computation())).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Integer> concatMap(){
        /*
         * 관찰한 observable 의 data를 변형한다.
         * map 과는 다르게, observable 로만 변형이 가능하고, 변형된 observable 안의 값이 subscriber에게 발행된다.
         * flatMap 과는 다르게, 순서가 보장된다.
         */
        return from().concatMap(integer -> Observable.just(integer * integer).subscribeOn(Schedulers.computation())).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<String> debounce(){
        /*
         * 마지막으로 아이템이 발행되고 난 후, 주어진 시간만큼이 지나고 나면 이벤트가 발생한다.
         * onComplete가 호출되면 그 즉시 마지막 아이템으로 이벤트가 발생한다.
         *
         * 이 예제에서는, debounce에서 주어진 시간은 2.5초이기 때문에 sleepTime이 2.5초보다 작을 경우 마지막 아이템인 c에 대해서만 이벤트가 발생할 것이다.
         * 그 외의 경우 a,b,c 모두 이벤트가 발생하게 될 것이다.
         */
        Random random = new Random(System.currentTimeMillis());
        int sleepTime = random.nextInt(4);

        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("a [sleep : " + sleepTime + "sec, debounce : 2.5sec]");
                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (InterruptedException e) {
                }
                subscriber.onNext("b [sleep : " + sleepTime + "sec, debounce : 2.5sec]");
                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (InterruptedException e) {
                }
                subscriber.onNext("c [sleep : " + sleepTime + "sec, debounce : 2.5sec]");
            }
        }).debounce(2500, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }






    /* subject */
    public void publishSubject(Action1<String> onNext){
        /*
         * 구독한 시점 이후 발행된 데이터부터 얻을 수 있다.
         * 즉, 6부터 얻을 수 있음.
         */
        PublishSubject<String> publishSubject = PublishSubject.create();
        Observable.range(1, 10).subscribe(integer -> {
            publishSubject.onNext(String.valueOf(integer));
            if(integer == 5){
                publishSubject.subscribe(onNext);
            }
        });
    }

    public void behaviorSubject(Action1<String> onNext){
        /*
         * 구독한 시점 바로 이전에 발행된 데이터부터 얻을 수 있다.
         * 즉, 5부터 얻을 수 있음.
         */
        BehaviorSubject<String> behaviorSubject = BehaviorSubject.create();
        Observable.range(1, 10).subscribe(integer -> {
            behaviorSubject.onNext(String.valueOf(integer));
            if(integer == 5){
                behaviorSubject.subscribe(onNext);
            }
        });
    }
}
