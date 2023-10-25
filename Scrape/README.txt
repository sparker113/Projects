Frac Field Engineer Automation Program, transfers full stage analysis report to local workbooks in which the path is configurable. 

Allows user to create "transfer templates" to customize data transfer to external entity workbooks; 

Backend data collection with accompanying server with front end data retrieval and analytic capabilities.
Saves all analyzed data locally and would send the serialized object containing the data to a "server", which was a laptop that was running a host program indefinitely that would serve as a server and backup the data files to a OneDrive directory that was synchronized locally.
This program also allowed any user on the office's network to download this data to their local machine and with a password authenticated by the server program, in which the data would be displayable within the program, transferrable to any workbook, analyzed on a well basis or a gross data basis within a timeframe. I was able to create a Pump Allocation Forecasting model that used the formation, county, and job design, where this historic data allowed me to effectively gather op. pressures, rates, stage perforation MD, and ISIP's organized by formation and county to easily determine reasonable/achievable friction pressure, lumping all encountered dynamic pressures together as the fluid's interaction with the wellbore, in order to allow the service company to define max pressures on a reservoir basis rather than an equipment limit basis, which would ultimately save the service company money in maintenance as well as the operater on downtime on equipment, and reducing risks of company reps. riding max pressure and cutting rate at a spike with high concentrations of sand, thus halting momentum and quickly screening out the fractures, maintaining wellbore integrity at perforations, and allowing the FR to work in the capacity of creating a fracture network suitable for the reservoir, assuming that was a consideration in choosing the FR by the completion engineer./n/r

Due to varying customer concessions for defining job start/end and intervals used for averages, the program allows the user to define the rules for these bounds.

Calls a Python exe that sends an email to addresses with attachments previously specified by the user and written to a txt file from which the exe reads.
Calls a Python exe that creates the service comp.'s PJR pdf of the reports created in the workbooks at the time the data is transferred'
Has plotting capabilities that I created in the GraphPanel package, but that was more for my visualization when debugging issues with bad data from the source. The plots that were used were created by a Python program written by a coworker.

Has material tracking capability in which deliveries and used sand is cached on a pad basis, with a visualization of silo fill and the silos that should be used for the following stage, but I never implemented this capability fully;

Tracked diesel usage, pumps online, # of pumps blending and their average blend ratio and just about any other metric you want to keep track of, and all of it is accompanied with the ability to organize and analyze that data.



**My very first Java class to ever write is the mainFrame class, and I didn't have any software development experience prior outside of scripting VBA macros and Matlab, so everything stems off of the mainFrame class, and it is fairly messy, and I was working non-stop to just get things working to get operations remote and never stopped working to be able to implement all of the other capabilities while also trying to help manage issues in the remote op. room, related and non-related to the program, and manage other projects, so I never did a full overhaul of this class since it was working. The project paints a pretty good picture of the learning curve from 0 to ~10 months while engulfing yourself with Java.
