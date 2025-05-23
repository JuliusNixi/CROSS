This file describes the history (like a sort of diary) of all my attempts to achieve a professional input handling in the client's CLI.

TL;DR
See it at the end of this file.

1. The first attempt was the easiest.
To have 2 threads, one reading the keyboard's input on the console, and another asynchronously printing the notifications received.
Problem: When a notification arrived, the prompt was graphically drawn again and appeared empty; instead, the typed characters remained in the stdin buffer. Example:
-> I'm typin
-> NOTIFICATION
->
On the third line the prompt appears empty, but of course it is not, and the buffer contanins "I'm typin".
So, if the user now digits "something else", on the enter press, the final command will be "I'm typinsomething else", that's quite misleading.

    1.2 New idea! Clean the stdin buffer on a new notification. 
    Problem: You cannot know if there's something in the stdin buffer or not before reading it.
    Java provides the ".avaible()" method on the streams, that seems perfect, but the docs grasps that on the stdin input it doesn't work, and in fact, after freaking out about why it wasn't working, I can say that they are right. Well, so I must read to know if there's something in the stdin buffer, but if it's empty, I will be blocked forever... ಠ_ಠ. 
    
        1.2.2 No problem, I will use an additional new one dedicated thread to do this dirty job. So I wasted my time in writing ad hoc classes to do this. So now I had 3 threads. The main, the reader and the notifier (sounds like "Il buono, il brutto, il cattivo").
        Problem: How to distinguish when the input is valid (i.e., coming from a real read) from when it is not (i.e., coming from a read performed only to clear the buffer)?

            1.2.2.2 I thought it was simple, just sync the threads. So, when a notification arrives, the dedicated notification thread, tells the other one (reader), to stop reading and discard the readed characters if present.
            Problem: Readings were incorrect, sometimes (but quite often) characters at the beginning were skipped (unread) when the user confirmed the command just before a notification. Example:
            -> hello (enter pressed)
            -> NOTIFICATION (arrived a little bit after sending 'hello').
            I read (contained in the buffer): 'llo'
            (Where the heck did the 'h' and 'e' go?!?)
            Thinking about it, it does what I told, but not how I imagined. I had imagined that the result in this case was simply all discarded, not truncated...
            I tought it could be a synchronization problem, well I still don't understand why it happens, I have tried many different approaches, synchronization with “simple implicit Locks”, signaling with “Signal()/Notify()”, sharing a “volatile AtomicBoolean invalidInput” variable and finally to terminate the reader thread when a notification arrives and restart it when it finishes. Nothing solved it, but now I think it's more a terminal timing I/O related problem that a synchronization one.

    1.3 Alternative idea! I will try to use JAVA NIO! From Internet "NIO adds [...] non-blocking mode – to read whatever is ready [...]". Curiosity, we were supposed to study it during the course, but this year, having had to do an introduction to Java, we didn't due to lack of time.
    Problem: Maybe I wasn't able to, but I couldn't get it to work and I read on the internet that non-blocking operations work on sockets, files, and more but it's NOT guaranteed to work on input terminals, and is PLATFORM DEPENDENT.

TL;DR
2. Very dejected with the last of my remaining energy, I discovered the existence of the JLine library that allows you to control the terminal in much more detail pretty easy. Also, with this, I am not forced to empty the stdin buffer, but can keep it and rewrite it to the screen after receiving a notification, making it even more beautiful and professional, AMAZING! Problem: NONE, FINALLY IT WORKS!

Note: The text is about a received notification (of executed orders) but the problem is still the same with any response received from the server (to the client's requests) if we permit to the user to continue writing commands without waiting for the response to the previous sent request. So the problem is much more relevant.
