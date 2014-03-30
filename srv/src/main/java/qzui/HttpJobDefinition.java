package qzui;

import com.github.kevinsawicki.http.HttpRequest;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import restx.factory.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.quartz.JobBuilder.newJob;

/**
 * Date: 19/2/14
 * Time: 06:34
 */
@Component
public class HttpJobDefinition extends AbstractJobDefinition {
    private static final Logger logger = LoggerFactory.getLogger(HttpJobDefinition.class);

    @Override
    public boolean acceptJobClass(Class<? extends Job> jobClass) {
        return jobClass.getName() == HttpJob.class.getName();
    }

    @Override
    public JobDescriptor buildDescriptor(JobDetail jobDetail, List<? extends Trigger> triggersOfJob) {
        HttpJobDescriptor jobDescriptor = setupDescriptorFromDetail(new HttpJobDescriptor(), jobDetail, triggersOfJob);

        return jobDescriptor
                .setUrl((String) jobDescriptor.getData().remove("url"))
                .setMethod((String) jobDescriptor.getData().remove("method"))
                .setBody((String) jobDescriptor.getData().remove("body"));
    }

    public static class HttpJobDescriptor extends JobDescriptor {

        private String url;
        private String method = "POST";
        private String body;

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public String getBody() {
            return body;
        }

        public HttpJobDescriptor setBody(final String body) {
            this.body = body;
            return this;
        }

        public HttpJobDescriptor setMethod(final String method) {
            this.method = method;
            return this;
        }

        public HttpJobDescriptor setUrl(final String url) {
            this.url = url;
            return this;
        }

        @Override
        public JobDetail buildJobDetail() {
            JobDataMap dataMap = new JobDataMap(getData());
            dataMap.put("url", url);
            dataMap.put("method", method);
            dataMap.put("body", body);
            return newJob(HttpJob.class)
                    .withIdentity(getName(), getGroup())
                    .usingJobData(dataMap)
                    .build();
        }

        @Override
        public String toString() {
            return "HttpJobDescriptor{" +
                    "url='" + url + '\'' +
                    ", method='" + method + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }

    }

    public static class HttpJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
        	TimeZone tz = TimeZone.getTimeZone("UTC");
        	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        	df.setTimeZone(tz);
        	String scheduledTimeInISO = df.format(context.getScheduledFireTime());
        	String firedTimeInISO = df.format(context.getFireTime());
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();            
            String url = jobDataMap.getString("url");
            url = url+"&scheduledTime="+scheduledTimeInISO+"&firedTime"+firedTimeInISO;
            
            String method = jobDataMap.getString("method");
            HttpRequest request = new HttpRequest(url, method);
            if (!isNullOrEmpty(jobDataMap.getString("body"))) {
                request.send(jobDataMap.getString("body"));
            }

            int code = request.code();
            String responseBody = request.body();
            logger.info("{} {} => {}\n{}", method, url, code, responseBody);
        }
    }

}
