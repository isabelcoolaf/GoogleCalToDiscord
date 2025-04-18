# Google Calendar to Discord  
*(A Discord status tracker for Google Calendar)*

## Description   
Google Calendar to Discord is a rich presence status that displays your activities from your Google Calendar. The rich presence will display the current event on the calendar, or a message when there are no current events.

## Installation

* Download the **GC2D** jar file.  
    
* Head over to the discord developer portal [**here**](https://discord.com/developers/applications)**.**  
    
* Create a new application, enter a name, and agree to the terms of service.  
    
* In General Information, copy down the application ID. *(you will need this later)*  
    
* In the left menu, select ’bot’ and click “reset token” and copy down the new token.  
    
* In the left menu, select ‘installation’ and uncheck “Guild Install”.  
    
* Hit the “copy” button under **Install Link**  and paste it into your browser or into Discord.  
    
* Click the authorize button. This will allow you to use slash commands with the application  
    
* Next you will need to create a google calendar api key by going [here](https://console.cloud.google.com/projectselector2/apis/credentials?authuser=4&supportedpurview=project&allowsmanagementprojects=true) and creating a new project.  
    
* When finished go to “Enabled APIs & services” in the left menu.  
    
* Click the “Enable APIs and services” button at the top and search for “Google Calendar”.  
    
* Select the “Google Calendar API” and click ‘enable’.  
    
* Finally, go to the “Credentials” tab on the left menu and hit “ Create Credentials” at the top and select “OAuth client ID”.  
    
* Select ‘Desktop app’ as the application type and provide a name then hit ‘Create’.  
    
* If it asks you to set up an OAuth consent screen then simply provide appropriate details *(don't worry, only you will end up seeing what you enter)*   
    
* Once finished, you will be provided with a client ID and secret. Copy these down.  
    
* Go to the ‘Audience’ tab on the left and click ‘Add users’ under test users and enter the email of the google account you want to see the calendar from and hit ‘Save’.  
    
* Go to the ‘Data Access’ tab on the left and click ‘Add or remove scopes’.  
    
* Enter the following into the bottom text field:   
  `https://www.googleapis.com/auth/calendar`  
    
* Click the update button at the bottom and then click the ‘Save’ button.  
    
* Now Run the **GC2D** jar file and it will ask you to provide the information you collected previously. Make sure there is no whitespace\!  
    
* Once all information is entered and you click the ‘save’ button, a browser tab will be opened asking which account you want to sign into. This is the account from where your calendars will come from.  
    
* If you enter something incorrectly then find and delete the .gc2d file on your computer. *(usually in the “Users” folder)*


## Features
  ### GUI
  - Diplays the current event and description.
  - Option to change calendar using the calendar selector.
  - Allows you to change the rich presence image.

  ### Slash Commands
A Variety of slash commands can be used to interact with and alter your rich presence status. 

* `presence-type`  
  	Changes the displayed presence type to ‘Playing’, ‘Watching’, or ‘Listening’  
* `select-calendar`  
  	Provides a list of calendars to select which one will be displayed  
* `next-event`  
  	Shows the next event on your calendar to you in a message  
* `start-next-event`  
  	Immediately stats the next calendar event and displays it as your presence
*	`select-images`  
		Provide a large and/or small image URL to display on the rich presence   
* `sleep`  
    Pauses rich presence updating for the selected number of days.  
* `reset`  
    Resets all calendar preferences. 
