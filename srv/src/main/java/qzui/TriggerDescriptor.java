package qzui;

import org.joda.time.DateTime;
import org.quartz.Trigger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Date: 18/2/14
 * Time: 21:55
 */
public class TriggerDescriptor {
    public static TriggerDescriptor buildDescriptor(Trigger trigger) {
        return new TriggerDescriptor()
                .setGroup(trigger.getKey().getGroup())
                .setName(trigger.getKey().getName())
                .setCron(trigger.getJobDataMap().getString("cron"))
                .setWhen(trigger.getJobDataMap().getString("when"))
                .setStartAt(trigger.getJobDataMap().getString("startAt"))
                .setEndAt(trigger.getJobDataMap().getString("endAt"));
                
    }

    private String name;
    private String group;

    private String when;
    private String cron;
    
    private String startAt;
    private String endAt;
    

    public Trigger buildTrigger() {
        if (!isNullOrEmpty(cron)) {        	
        	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
				return newTrigger()
				        .withIdentity(buildName(), group)
				        .withSchedule(cronSchedule(cron))
				        .startAt(formatter.parse(startAt))
				        .endAt(formatter.parse(endAt))
				        .usingJobData("cron", cron)
				        .build();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IllegalStateException("Exception while parsing startAt/endAt date " + this);
			}
        } else if (!isNullOrEmpty(when)) {
            if ("now".equalsIgnoreCase(when)) {
                return newTrigger()
                        .withIdentity(buildName(), group)
                        .usingJobData("when", when)
                        .build();
            }

            DateTime dateTime = DateTime.parse(when);
            return newTrigger()
                    .withIdentity(buildName(), group)
                    .startAt(dateTime.toDate())
                    .usingJobData("when", when)
                    .build();
        }
        throw new IllegalStateException("unsupported trigger descriptor " + this);
    }

    private String buildName() {
        return isNullOrEmpty(name) ? "trigger-" + UUID.randomUUID() : name;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getWhen() {
        return when;
    }

    public String getCron() {
        return cron;
    }

    public TriggerDescriptor setName(final String name) {
        this.name = name;
        return this;
    }

    public TriggerDescriptor setGroup(final String group) {
        this.group = group;
        return this;
    }

    public TriggerDescriptor setWhen(final String when) {
        this.when = when;
        return this;
    }

    public TriggerDescriptor setCron(final String cron) {
        this.cron = cron;
        return this;
    }
    public TriggerDescriptor setStartAt(final String startAt) {
        this.startAt = startAt;
        return this;
    }
    
    public TriggerDescriptor setEndAt(final String endAt) {
        this.endAt = endAt;
        return this;
    }
    

    @Override
    public String toString() {
        return "TriggerDescriptor{" +
                "name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", when='" + when + '\'' +
                ", cron='" + cron + '\'' +
                ", endAt='" + endAt + '\'' +
                ", startAt='" + startAt + '\'' +
                '}';
    }
}
