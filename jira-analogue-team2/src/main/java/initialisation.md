# Kuidas töötab initsialiseerimine?

Väike ülevaade.

a) Kliendis.

b) Serveri käivitamisel.

## Leiduvad järgmised "seosed"
```
Project{
	List<Task> tasklist; (ARRAY MEMBERS)
}
Task{
    User createdby; (ID)
    Task masterTask; (ID)
    List<User> employees; (ID SORT)
    List<Project> projects; (ID SORT)
}
User{
    List<Project> projects; (ID SORT)
    HashMap<Project, Integer> projectRights; (ARRAY INTEGER ENUMS)
    List<User> friendList; (ID SORT)
}
```
Initsialiseerimisprotseduur.
```
read raw projects, raw users
	NB: raw projects contain raw tasks.

recursively convert raw types to normal ones	

fn initialize project ()
	initialize member tasks (tasks, users projects)

fn initialize task (tasks, users, projects)
	add masterTasks to this
	add employees to this
	add projects to this

fn initialize user (users, projects):
	add friends to this
	add projects to this
	add projectrights to this
```
Serialiseerimisprotseduur
TODO