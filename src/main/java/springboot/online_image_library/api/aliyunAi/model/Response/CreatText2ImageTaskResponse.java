package springboot.online_image_library.api.aliyunAi.model.Response;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Yu'S'hui'shen
 */
@Data
public class CreatText2ImageTaskResponse {

    @JsonProperty("request_id")
    private String requestId;
    private Output output;
    private Usage usage;

    @Data
    public static class Output {
        @JsonProperty("task_id")
        private String taskId;

        @JsonProperty("task_status")
        private String taskStatus;

        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("submit_time")
        private String submitTime;

        @JsonProperty("scheduled_time")
        private String scheduledTime;

        @JsonProperty("end_time")
        private String endTime;

        private List<Result> results;

        @JsonProperty("task_metrics")
        private TaskMetrics taskMetrics;
    }

    @Data
    public static class Result {
        private String url;
    }

    @Data
    public static class TaskMetrics {
        /**
         * 总任务数
         */
        @Alias("TOTAL")
        private Integer total;

        /**
         * 成功任务数
         */
        @Alias("SUCCEEDED")
        private Integer succeeded;

        /**
         * 失败任务数
         */
        @Alias("FAILED")
        private Integer failed;
    }

    @Data
    public static class Usage {
        @JsonProperty("image_count")
        private int imageCount;
    }
}
