package ch.qos.logback.core.rolling;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 日志滚动
 * Created by guozhenbin on 2017/7/18.
 */
public class SeanRollingFileAppender<E> extends RollingFileAppender<E> {
    private static List<SeanRollingFileAppender> appenders = new CopyOnWriteArrayList();

    private void checkRollover() {
        TriggeringPolicy var1 = this.triggeringPolicy;
        synchronized(this.triggeringPolicy) {
            if(this.triggeringPolicy.isTriggeringEvent(this.currentlyActiveFile, null)) {
                this.rollover();
            } else {
                if(this.triggeringPolicy instanceof TimeBasedRollingPolicy && !this.currentlyActiveFile.exists()) {
                    TimeBasedFileNamingAndTriggeringPolicy policy = ((TimeBasedRollingPolicy)this.triggeringPolicy).getTimeBasedFileNamingAndTriggeringPolicy();
                    if(policy instanceof TimeBasedFileNamingAndTriggeringPolicyBase) {
                        TimeBasedFileNamingAndTriggeringPolicyBase tp = (TimeBasedFileNamingAndTriggeringPolicyBase)policy;
                        tp.elapsedPeriodsFileName = tp.tbrp.fileNamePatternWCS.convert(tp.dateInCurrentPeriod);
                        this.rollover();
                    }
                }

            }
        }
    }

    public SeanRollingFileAppender() {
        appenders.add(this);
    }

    static {
        Executors.newScheduledThreadPool(5).scheduleWithFixedDelay(new TimerTask() {
            public void run() {
                String name = Thread.currentThread().getName();

                try {
                    Thread.currentThread().setName("SeanLogbackTimer");
                    Iterator i$ = SeanRollingFileAppender.appenders.iterator();

                    while(i$.hasNext()) {
                        SeanRollingFileAppender appender = (SeanRollingFileAppender)i$.next();
                        appender.checkRollover();
                    }
                } finally {
                    Thread.currentThread().setName(name);
                }

            }
        }, 1L, 1L, TimeUnit.MINUTES);
    }
}
