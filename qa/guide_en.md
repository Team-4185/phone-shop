## How to test the code?

For testing the code, we decided to use [Docker](https://www.docker.com/), which lets you start the whole
application
together with the database locally in one go.
We decided not to test the application directly in the `prod` environment because we understand that testing involves
a large number of data manipulations, which would simply fill the production database with unnecessary data.
For every testing task, you will need to start the whole application
using [Docker](https://www.docker.com/)
from scratch, but for that you only need to run one script: `setup.sh` or `setup.bat` (depending on your
operating system).

It is important to understand that this way you are testing exactly the **backend side** and its API.

### What am I starting?

When you run the `setup.sh` or `setup.bat` script, it starts four containers:<br>
**The first container** is the [Postgres](https://www.postgresql.org/) database, which is already minimally configured.<br>
**The second container** is [Redis](https://redis.io/), which the backend needs to check revoked access and refresh tokens.<br>
**The third container** is [MinIO](https://min.io/), where product images are stored.<br>
**The fourth container** is the application itself, developed by the `backend` developers, and this is what you need to test.

These containers are already connected to each other, and they already interact with each other correctly.

> You can freely connect to the database and interact with it. But it is important to understand
> that database data is stored even after you
> delete the [container](https://docs.docker.com/get-started/docker-concepts/the-basics/what-is-a-container/).

### What is required?

To start the applications correctly, you need:

- [Docker Desktop](https://docs.docker.com/desktop/setup/install/windows-install/)
  installed and running on your computer - this is what we use to start everything.
- [Git](https://git-scm.com/downloads) installed on your computer - we use it to pull the latest version
  of the project.
- [Environment variables](https://en.wikipedia.org/wiki/Environment_variable) -
  these are special variables where we specify values when starting our application. These variables are stored
  in a special file. It is important to understand that different application versions may use different environment
  variable files.

> Environment variables should be taken from a backend developer or QA mentor.

### I already have everything, let's start

So, once again make sure that you already have all required programs installed on your PC,
and also have a file with all environment variables (`test.env`).
**Steps:**

1. For convenient work, it is recommended to create a separate working folder on your desktop. It does not matter what
   you call it.
2. Next, go to the working directory.
3. Open the `Git` terminal:
    - right-click an empty area inside the opened folder
    - if Git is installed on your PC, you should have the `Open Git Bash here` option.
    - click this option, and the terminal for working with Git will open.
4. Enter the command `git clone https://github.com/Team-4185/phone-shop.git` and wait for the download to finish.
5. After that, make sure that a folder named `phone-shop` appeared in the working directory.
6. After that, go to the `phone-shop -> qa` directory and make sure you see files named `setup`.
7. Put the environment variable file named `test.env` into the `phone-shop` or `phone-shop/qa` directory.
8. Make sure **Docker Desktop** is running.
9. Run the script:
    - if you use Windows OS:
        - run the `setup.bat` file as administrator
        - wait until a container named `gadget-room-backend` appears in the **Docker Desktop** Containers tab
        - start working.
    - if you use Linux OS:
        - open a terminal in this directory
        - enter the command `sudo chmod +x ./setup.sh`
        - enter the command `./setup.sh`
        - wait until a container named `gadget-room-backend` appears in the **Docker Desktop** Containers tab
        - start working.
10. Open `http://localhost:8080/swagger-ui.html` in a browser.

> **Note:** If after updating the branch the container does not start because of old data or a Flyway checksum mismatch,
  do not delete containers manually in **Docker Desktop**. Run `./setup.sh reset` on Linux/macOS or
  `setup.bat reset` on Windows. This command deletes Docker volumes only for the `gadget-room-backend`
  project and starts a clean environment again.

### How do I connect to the database?

To connect to the database, you need any PostgreSQL client. We recommend using
one of the following:

- [DBeaver](https://dbeaver.io/download/) (free and convenient)
- [pgAdmin](https://www.pgadmin.org/download/) (official PostgreSQL client)
- [DataGrip](https://www.jetbrains.com/datagrip/) (paid, from JetBrains)

**Connection parameters:**

| Parameter | Value |
|:----------|-------|
| Host | `localhost` |
| Port | value of `DB_PORT` from `test.env` |
| Database | value of `DB_NAME` from `test.env` |
| Username | value of `DB_USERNAME` from `test.env` |
| Password | value of `DB_PASSWORD` from `test.env` |

**DBeaver connection example:**

1. Open DBeaver.
2. Click `Database` → `New Database Connection`.
3. Select `PostgreSQL` → `Next`.
4. Fill in the fields:
    - **Host:** `localhost`
    - **Port:** value of `DB_PORT` from `test.env`
    - **Database:** value of `DB_NAME` from `test.env`
    - **Username:** value of `DB_USERNAME` from `test.env`
    - **Password:** value of `DB_PASSWORD` from `test.env`
5. Click `Test Connection` to verify it.
6. Click `Finish`.

> **Important:** The database is available only while the Docker container `gadget-room-backend` is running. If the
  container is stopped, you will not be able to connect.

> **Note:** Database data is stored even after container restarts thanks to the Docker
  volume `gadget-room-postgres-volume`.

### How do I view the image storage?

For storing images,
we use [minio](https://mivocloud.com/ru/blog/CHto-takoe-obiektnoe-hranilishche-Minio-i-kak-ono-rabotaet).
**Minio** is an open-source lightweight file storage. It is easy to start and easy and clear to work with. Moreover,
its API
is designed in such a way that it satisfies the [S3](https://ru.wikipedia.org/wiki/Amazon_S3) specification,
and it can be replaced with the [Amazon version](https://aws.amazon.com/ru/s3/?nc=sn&loc=0) (or vice versa).
Currently **minio** is being commercialized, so you may not find it on the [official resources](https://www.min.io/),
because they are trying to hide it and sell you their new product [AIStor](https://www.min.io/), which is essentially
a paid version of **Minio**.

**Minio** starts a server where all images are stored. If you want to work with the server directly,
use this URL:

```
http://localhost:9000
```

For convenient manual work, there is also a client running on this port:

```
http://localhost:9001
```
After the first request to store an image, a [bucket](https://www.techtarget.com/searchaws/definition/AWS-bucket)
named `images` will be created, where all images will be stored.

Minio credentials (valid for both the client and the server):

| Parameter | Value |
|-----------|-------|
| Username | value of `MINIO_USERNAME` from `test.env` |
| Password | value of `MINIO_PASSWORD` from `test.env` |

> **Important:** Minio storage is available only while the Docker container `gadget-room-backend` is running. If the
  container is stopped, you will not be able to connect.

> **Note:** Database data is stored even after container restarts thanks to the Docker
  volume `gadget-room-minio-volume`.

### Which available endpoints can I view?

You have several options:

- `http://localhost:8080/swagger-ui.html` - get a visual view of the whole API
- `http://localhost:8080/v3/api-docs` - get a JSON object with all endpoints
