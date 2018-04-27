package cn.zengjingfang.box.android.rxhandler.rx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cn.zengjingfang.box.android.rxhandler.rx.Subscription;

/**
 *
 * Created by ZengJingFang on 2018/4/27.
 */

public class SubscriptionList implements Subscription{

    private List<Subscription> subscriptions;
    private volatile boolean unsubscribed;

    /**
     * Constructs an empty SubscriptionList.
     */
    public SubscriptionList() {
        // nothing to do
    }

    /**
     * Constructs a SubscriptionList with the given initial child subscriptions.
     * @param subscriptions the array of subscriptions to start with
     */
    public SubscriptionList(final Subscription... subscriptions) {
        this.subscriptions = new LinkedList<Subscription>(Arrays.asList(subscriptions));
    }

    /**
     * Constructs a SubscriptionList with the given initial child subscription.
     * @param s the initial subscription instance
     */
    public SubscriptionList(Subscription s) {
        this.subscriptions = new LinkedList<Subscription>();
        this.subscriptions.add(s);
    }

    @Override
    public boolean isUnsubscribed() {
        return unsubscribed;
    }

    /**
     * Adds a new {@link Subscription} to this {@code SubscriptionList} if the {@code SubscriptionList} is
     * not yet unsubscribed. If the {@code SubscriptionList} <em>is</em> unsubscribed, {@code add} will
     * indicate this by explicitly unsubscribing the new {@code Subscription} as well.
     *
     * @param s
     *          the {@link Subscription} to add
     */
    public void add(final Subscription s) {
        if (s.isUnsubscribed()) {
            return;
        }
        if (!unsubscribed) {
            synchronized (this) {
                if (!unsubscribed) {
                    List<Subscription> subs = subscriptions;
                    if (subs == null) {
                        subs = new LinkedList<Subscription>();
                        subscriptions = subs;
                    }
                    subs.add(s);
                    return;
                }
            }
        }
        // call after leaving the synchronized block so we're not holding a lock while executing this
        s.unsubscribe();
    }

    public void remove(final Subscription s) {
        if (!unsubscribed) {
            boolean unsubscribe;
            synchronized (this) {
                List<Subscription> subs = subscriptions;
                if (unsubscribed || subs == null) {
                    return;
                }
                unsubscribe = subs.remove(s);
            }
            if (unsubscribe) {
                // if we removed successfully we then need to call unsubscribe on it (outside of the lock)
                s.unsubscribe();
            }
        }
    }

    /**
     * Unsubscribe from all of the subscriptions in the list, which stops the receipt of notifications on
     * the associated {@code Subscriber}.
     */
    @Override
    public void unsubscribe() {
        if (!unsubscribed) {
            List<Subscription> list;
            synchronized (this) {
                if (unsubscribed) {
                    return;
                }
                unsubscribed = true;
                list = subscriptions;
                subscriptions = null;
            }
            // we will only get here once
            unsubscribeFromAll(list);
        }
    }

    private static void unsubscribeFromAll(Collection<Subscription> subscriptions) {
        if (subscriptions == null) {
            return;
        }
        List<Throwable> es = null;
        for (Subscription s : subscriptions) {
            try {
                s.unsubscribe();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<Throwable>();
                }
                es.add(e);
            }
        }
        throwIfAny(es);
    }
    /* perf support */
    public void clear() {
        if (!unsubscribed) {
            List<Subscription> list;
            synchronized (this) {
                list = subscriptions;
                subscriptions = null;
            }
            unsubscribeFromAll(list);
        }
    }
    /**
     * Returns true if this composite is not unsubscribed and contains subscriptions.
     * @return {@code true} if this composite is not unsubscribed and contains subscriptions.
     */
    public boolean hasSubscriptions() {
        if (!unsubscribed) {
            synchronized (this) {
                return !unsubscribed && subscriptions != null && !subscriptions.isEmpty();
            }
        }
        return false;
    }

    public static void throwIfAny(List<? extends Throwable> exceptions) {
        if (exceptions != null && !exceptions.isEmpty()) {
            if (exceptions.size() == 1) {
                Throwable t = exceptions.get(0);
                // had to manually inline propagate because some tests attempt StackOverflowError
                // and can't handle it with the stack space remaining
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new RuntimeException(t); // NOPMD
                }
            }
//            throw new CompositeException(exceptions);
            // TODO: 2018/4/27
        }
    }
}
