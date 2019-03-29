import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task {
    private static final String KEY_TASKID = "taskId";
    private static final String KEY_COMPLETED = "isCompleted";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PRIORITY = "taskPriority";
    private static final String KEY_CREATEDBY = "createdBy";
    private static final String KEY_DEADLINE = "deadline";
    private static final String KEY_CREATED = "dateCreated";
    private static final String KEY_MASTERTASK = "masterTask";
    private static final String KEY_EMPLOYEES = "assignedEmployees";

    private long taskId;
    private boolean isCompleted = false;
    private String title = "";
    private String description = "";
    private int priority = -1;
    private long createdBy = -1;
    private Date deadline = null;
    private Date created = null;
    private Task masterTask = null;
    private long masterTaskId = -1;
    private List<User> employees = new ArrayList<>();
    private long[] empolyeeIds = null;

    public static Task fromJson(JSONObject object) {
        Task result = new Task();

        if (object.containsKey(KEY_TASKID))
            result.taskId = (Long) object.get(KEY_TASKID);
        else return null;
        if (object.containsKey(KEY_COMPLETED))
            result.isCompleted = (Boolean) object.get(KEY_COMPLETED);
        if (object.containsKey(KEY_TITLE))
            result.title = (String) object.get(KEY_TITLE);
        if (object.containsKey(KEY_DESCRIPTION))
            result.description = (String) object.get(KEY_DESCRIPTION);
        if (object.containsKey(KEY_PRIORITY))
            result.priority = (Integer) object.get(KEY_PRIORITY);

        if (object.containsKey(KEY_CREATEDBY))
            result.createdBy = (Integer) object.get(KEY_CREATEDBY);
        if (object.containsKey(KEY_DEADLINE))
            result.deadline = new Date((Integer) object.get(KEY_DEADLINE));
        if (object.containsKey(KEY_CREATED))
            result.created = new Date((Integer) object.get(KEY_CREATED));
        if (object.containsKey(KEY_MASTERTASK))
            result.masterTaskId = (Integer) object.get(KEY_MASTERTASK);

        if (object.containsKey(KEY_EMPLOYEES) && object.get(KEY_EMPLOYEES) instanceof JSONArray) {
            JSONArray array = (JSONArray) object.get(KEY_EMPLOYEES);
            result.empolyeeIds = new long[array.size()];
            int i = 0;
            for (Object o : array) {
                result.empolyeeIds[i] = (Long) o;
                i++;
            }
        }
        return result;
    }

    public static JSONObject toJson(Task object) {
        return null;
    }
}
