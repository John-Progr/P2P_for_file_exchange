# P2P_for_file_exchange
In this project i make a simple peer-to-peer distributed system for file exchange
The system consists of one tracker and multiple peers.Each peer has an account on tracker and a shared_directory,which they use to save files they prefer to share with other peers.
Once a peer connects to the tracker(with username and password),he updates him with the files that are availiable
When a peer wants to download a file,it requests and receives information concerning ip,port of peers having the file and then he procceeds to connect to one of them so he can download it.Point here is the use of multithreading

So from technical perspective

Peer has 3 basic operations
1.Register->Peer chooses a username and password and he sends them to tracker.System checks if the username is already in use.if not, then it returns a message of success to peer and he is registered,otherwise peer has to choose different name

2.Login->Peer 

3.Logout->Peer updates tracker that he wants to quit,tracker accepts it and proceeds to update his data structures 

Peer also has other operations concerning the file exchange,these are:
1.list->It requests from tracker the availiable files found on the P2P System

2.details->Peer requests from tracker specific information about a particular file which is found on the list above.Tracker replys with a list of information containing ip_address,port,user_name,count_downloads,count_failures of peers who have the this file

3.checkActive->

4.SimpleDownload->Peer after the search of file picks the best peer from details operation based on two factors

                1. the response time for each peer,having the file
                2.0.9







