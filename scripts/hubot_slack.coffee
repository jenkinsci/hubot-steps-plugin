# Description:
#   Interact with hubot itself.
#
# Dependencies:
#   None
#
# Configuration:
#   None

# Commands:
#   None

# URLS:
#   POST /hubot/notify/<room> (message=<message>)
#
# Author:
#   Fabric8, nrayapati

module.exports = (robot) ->

  fs = require 'fs'
  fs.exists './logs/', (exists) ->
    if exists
      startLogging()
    else
      fs.mkdir './logs/', (error) ->
        unless error
          startLogging()
        else
          console.log "Could not create logs directory: #{error}"
  startLogging = ->
    console.log "Started ChatOps HTTP Script logging"
    robot.hear //, (msg) ->
      fs.appendFile logFileName(msg), formatMessage(msg), (error) ->
        console.log "Could not log message: #{error}" if error
  logFileName = (msg) ->
    safe_room_name = "#{msg.message.room}".replace /[^a-z0-9]/ig, ''
    "./logs/#{safe_room_name}.log"
  formatMessage = (msg) ->
    "[#{new Date()}] #{msg.message.user.name}: #{msg.message.text}\n"

  robot.router.post '/hubot/notify/:room', (req, res) ->
    room = req.params.room

    # Actual message.
    message = req.body.message
    # Status STARTED/ABORTED/SUCCESS/FAILURE/NOT_BUILT/BACK_TO_NORMAL/UNSTABLE for build notifications
    # And for pipeline steps what ever user sends, by default to SUCCESS.
    status = req.body.status
    # extraData is empty for build notifications, but what ever user sends for pipelines.
    extraData = req.body.extraData
    # User Id, null for anonymous.
    userId = req.body.userId
    # User Name
    userName = req.body.userName
    # Defaults to User Name for the builds kicked off by users for others actual build cause. Example TimerTrigger, SCMChange and so on.
    buildCause = req.body.buildCause
    # BUILD - For Build notifications.
    # SEND - hubotSend
    # APPROVE - hubotApprove
    # TEST - For hubot site test notifications.
    stepName = req.body.stepName

    # Except test notifications, this list of envrionment variable available for the current build.
    envVars = req.body.envVars

    # Current time in milliseconds.
    ts = req.body.ts / 1000

    # hubotApprove step vars
    # input id
    id = req.body.id
    # submitter
    submitter = req.body.submitter
    submitterParameter = req.body.submitterParameter
    ok = req.body.ok
    # TODO Yet to add this server side.
    # parameters = req.body.parameters


    # Validate Site for both folder level and global site level.
    if stepName == 'TEST'
      if extraData.FOLDER_NAME
        attachments = [ { "color": "#1093E8", "text": message, "title": "Jenkins » " + extraData.FOLDER_NAME[0].toUpperCase() + extraData.FOLDER_NAME[1..-1], "title_link": extraData.FOLDER_URL, "footer": userName, "footer_icon": "https://png.icons8.com/color/1600/jenkins.png", "ts": ts }]
      else
        attachments = [ { "color": "#1093E8", "text": message, "title": "Jenkins » Global", "title_link": extraData.JENKINS_URL, "footer": userName, "footer_icon": "https://png.icons8.com/color/1600/jenkins.png", "ts": ts }]
    # else for all other cases like BUILD/SEND/APPROVE
    else
      if status == 'FAILURE'
        color = 'danger'
      else if status == 'ABORTED'
        color = 'warning'
      else if status == 'STARTED'
        color = '#1093E8'
      else
        color = 'good'
      jobName = (envVars.JOB_NAME.split('/').map (word) -> word[0].toUpperCase() + word[1..-1]).join ' » '
      title = "Jenkins » " + jobName + " " + envVars.BUILD_DISPLAY_NAME

      if stepName == 'APPROVE'
        jobUrl = envVars.BUILD_URL.replace(envVars.JENKINS_URL, '')
        attachments = [ { "color": color, "text": message + "\n     *to Proceed reply:* `.j proceed " + jobUrl + "`" + "\n     *to Abort reply:* `.j abort " + jobUrl + "`", "title": title, "title_link": envVars.RUN_DISPLAY_URL, "footer": buildCause, "footer_icon": "https://png.icons8.com/color/1600/jenkins.png", "ts": ts, "mrkdwn_in": ["text", "pretext"] }]
      else
        attachments = [ { "color": color, "text": message, "title": title, "title_link": envVars.RUN_DISPLAY_URL, "footer": buildCause, "footer_icon": "https://png.icons8.com/color/1600/jenkins.png", "ts": ts, "mrkdwn_in": ["text", "pretext"] }]

    robot.adapter.client.web.chat.postMessage(room, "", {as_user: true, unfurl_links: true, attachments: attachments })
    res.end()
