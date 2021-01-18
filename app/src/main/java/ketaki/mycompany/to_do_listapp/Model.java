package ketaki.mycompany.to_do_listapp;

public class Model {
    private String task , id;

    public Model ()
    {

    }
    public Model(String task, String id) {
        this.task = task;
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
