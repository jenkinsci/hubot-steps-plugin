# Hubot Pipeline Steps

Jenkins Pipeline Steps for integration with the [Hubot Chat Bot](https://hubot.github.com/) and human approval, inspired by the idea of [Fabric8 Workflow Steps](https://github.com/fabric8io/fabric8-jenkins-workflow-steps).

## Configuration

The following Jenkins pipeline steps are available if you add this plugin to your Jenkins:

### hubotSend

Allows sending of a message to the hubot chat bot

```
hubotSend room: 'release', message: 'Releasing this project.', url: 'http://localhost:9999', failOnError: true
```

`url` is optional, if it is provided as global environment variable `HUBOT_URL` or provided by `withEnv` step, this should always end with `/`.

`room` is optional, if it is provided as global environment variable `HUBOT_DEFAULT_ROOM` or provided by `withEnv` step, and room doesn't require `#`, it is added in the code.

`failOnError` is optional and by default it is false, which is if any error it won't abort the job., it can also be provided as global variable `HUBOT_FAIL_ON_ERROR`. 
 
### hubotApprove

Sends a hubot message the project chat room for a project when the build is waiting for user input with the hubot commands to proceed or abort the build.

```
hubotApprove room: 'release', message: 'Proceed with building this Job?', url: 'http://localhost:9999/', failOnError: true
```

`url` is optional, if it is provided as global environment variable `HUBOT_URL` or provided by `withEnv` step, this should always end with `/`.

`room` is optional, if it is provided as global environment variable `HUBOT_DEFAULT_ROOM` or provided by `withEnv` step, and room doesn't require `#`, it is added in the code.

`failOnError` is optional and by default it is false, which is if any error it won't abort the job., it can also be provided as global variable `HUBOT_FAIL_ON_ERROR`. 

```groovy
node {
    // Note: HUBOT_URL and HUBOT_DEFAULT_ROOM set as global environment variables.
    stage('Build') {
        hubotApprove message: 'Proceed with building this job?'
    }
}
```
<img src="images/slack.png" width="900">

Jenkins Approved Job:
<img src="images/proceed.png" width="500">

Jenkins Aborted Job:
<img src="images/abort.png" width="500">

## Hubot Setup

#### New to hubot? 

Refer to [hubot-base](https://github.com/ThoughtsLive/hubot-base) to setup a either docker container or to run it locally. 

#### Already running hubot.

Then just copy over following scripts from [hubot-base](https://github.com/ThoughtsLive/hubot-base) repo. 

* [hubot](https://github.com/ThoughtsLive/hubot-base/blob/master/scripts/hubot.coffee)
* [jenkins](https://github.com/ThoughtsLive/hubot-base/blob/master/scripts/jenkins.coffee)
