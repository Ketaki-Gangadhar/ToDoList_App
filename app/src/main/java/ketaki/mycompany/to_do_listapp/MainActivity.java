package ketaki.mycompany.to_do_listapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private TextView task_text;
    private RecyclerView my_recycler_view;
    private FloatingActionButton add_button;
    private ProgressDialog loader;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    private String key = "";
    private String tasks = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        task_text = findViewById(R.id.textView);
        my_recycler_view = findViewById(R.id.taskRecyclerView);
        add_button = findViewById(R.id.addButton);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        loader = new ProgressDialog(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        my_recycler_view.setHasFixedSize(true);
        my_recycler_view.setLayoutManager(linearLayoutManager);

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });


    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.new_task, null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final EditText task = myView.findViewById(R.id.newTaskText);
        Button Save = myView.findViewById(R.id.saveButton);
        Button Cancel = myView.findViewById(R.id.cancelButton);

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mTask = task.getText().toString().trim();

                if (TextUtils.isEmpty(mTask)) {
                    task.setError("Task is required!");
                    return;
                } else {
                    loader.setMessage("Adding task");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    String id = ref.push().getKey();
                    Model model = new Model(mTask, id);
                    ref.child(model.getId()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Task is added", Toast.LENGTH_SHORT).show();

                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(MainActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();

                        }
                    });

                    dialog.dismiss();
                }

            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                Log.i("msg", "task is " + map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("msg", "Failed to read value." + error.toException());
            }
        });
    }
  @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(ref,Model.class).build();

        FirebaseRecyclerAdapter<Model, myViewHolder> adapter = new FirebaseRecyclerAdapter<Model, myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Model model) {
                holder.setTask(model.getTask());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        tasks = model.getTask();
                        updateTask();
                    }
                });

            }
            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout,parent,false);
               return new myViewHolder(view);

            }




        };
        my_recycler_view.setAdapter(adapter);
        adapter.startListening();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public myViewHolder (@NonNull View itemView)
        {
            super(itemView);
            mView=itemView;
        }
        public void setTask (String task)
        {
            TextView taskText = mView.findViewById(R.id.taskName);
            taskText.setText(task);
        }
    }

  private void updateTask()
  {
      AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
      LayoutInflater inflater = LayoutInflater.from(this);
      View view = inflater.inflate(R.layout.update, null);
      myDialog.setView(view);

      AlertDialog dialog = myDialog.create();
      EditText kTask = view.findViewById(R.id.update_task_text);

      kTask.setText(tasks);
      kTask.setSelection(tasks.length());
      Button cancel = view.findViewById(R.id.cancel_button);
      Button update = view.findViewById(R.id.update_button);
      Button delete = view.findViewById(R.id.delete_Button);

      update.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              tasks = kTask.getText().toString().trim();
              Model model = new Model(tasks,key);
              ref.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> taskt) {
                      if(taskt.isSuccessful())
                      {
                          Toast.makeText(MainActivity.this, "Task has been updated successfully", Toast.LENGTH_SHORT).show();
                      }
                      else
                      {
                          String err = taskt.getException().toString();
                          Toast.makeText(MainActivity.this, "Update failed :" + err, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
              dialog.dismiss();
          }
      });

      cancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              dialog.dismiss();
          }
      });
      delete.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              ref.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> taskR) {
                      if(taskR.isSuccessful())
                      {
                          Toast.makeText(MainActivity.this, "Task has been deleted", Toast.LENGTH_SHORT).show();
                      }
                      else
                      {
                          String err = taskR.getException().toString();
                          Toast.makeText(MainActivity.this, "Delete failed :" + err, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
              dialog.dismiss();
          }

      });
      dialog.show();


  }

}