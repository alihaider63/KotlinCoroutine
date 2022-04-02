//CoroutineContext

fun main(): Unit = runBlocking { //Thread: main

    //this: CoroutineScope instance
    //coroutineContext: CoroutineContext instance

    /* Without Parameter:  CONFINED   [CONFINED DISPATCHER]
        - Inherits CoroutineContext from immediate parent coroutine.
        - Even after delay() or suspending functions , it continues to run in the same Thread.  */

    launch {
        println("C1: ${Thread.currentThread().name}") //Thread: main
        delay(1000)
        println("C1: ${Thread.currentThread().name}") //Thread: main
    }

    /* With Parameter:  Dispatcher.Default   [similar to GlobalScope.launch {} ]
        - Gets its own context at Global level. Executes in a separate background Thread.
        - Even after delay() or suspending functions,
            it continues to run either in the same Thread or some other Thread.  */

    launch(Dispatchers.Default) {
        println("C2: ${Thread.currentThread().name}") //Thread: T1
        delay(1000)
        println("C2: ${Thread.currentThread().name}") //Thread: T1 or some other Thread
    }

    /* Without Parameter:  Dispatcher.Unconfined   [UNCONFINED DISPATCHER]
    - Inherits CoroutineContext from immediate parent coroutine
    - Even after delay() or suspending functions, it continues to run in some other Thread   */

    launch(Dispatchers.Unconfined) {
        println("C3: ${Thread.currentThread().name}") //Thread: main
        delay(1000)
        println("C3: ${Thread.currentThread().name}") //Thread: some other thread  T2


        //Using coroutineContext property to flow context from parent to child.
        launch(coroutineContext) {
            println("C5: ${Thread.currentThread().name}") //Thread: T2
            delay(1000)
            println("C5: after delay ${Thread.currentThread().name}") //Thread: T2
        }
    }

    //Using coroutineContext property to flow context from parent to child.
    launch(coroutineContext) {
        println("C4: ${Thread.currentThread().name}") //Thread: main
        delay(1000)
        println("C4: ${Thread.currentThread().name}") //Thread: main
    }
}



//CoroutineScope

fun main() = runBlocking {

    println("runBlocking: $this")

    launch {
        println("launch: $this")

        launch {
            println("child launch: $this")
        }
    }
    async {
        println("async: $this")
    }

    println("...some other code.....")

}





//Concurrent Execution or parallel Execution by lazy coroutine

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val msgOne: Deferred<String> = async(start = CoroutineStart.LAZY) { getMessageOne() }
    val msgTwo: Deferred<String> = async(start = CoroutineStart.LAZY) { getMessageTwo() }

    println("The Entire Message is: ${msgOne.await() + msgTwo.await()}")

    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}

suspend fun getMessageOne(): String {
    delay(1000L) //Pretend to do some work
    println("After working in getMessageOne()")
    return "Hello "
}

suspend fun getMessageTwo(): String {
    delay(1000L) //Pretend to do some work
    println("After working in getMessageTwo()")
    return "World!"
}





//Concurrent Execution or parallel Execution by launch builder

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val time = measureTimeMillis {
        val msgOne = launch { getMessageOne() }
        val msgTwo = launch { getMessageTwo() }
        //println("The Entire Message is: ${msgOne.await() + msgTwo.await()}")
    }

    println("Completed in $time ms")
    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}

suspend fun getMessageOne(): String {
    delay(1000L) //Pretend to do some work
    return "Hello "
}

suspend fun getMessageTwo(): String {
    delay(1000L) //Pretend to do some work
    return "World!"
}




//Concurrent Execution or parallel Execution by async builder

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val time = measureTimeMillis {
        val msgOne: Deferred<String> = async { getMessageOne() }
        val msgTwo: Deferred<String> = async { getMessageTwo() }
        println("The Entire Message is: ${msgOne.await() + msgTwo.await()}")
    }

    println("Completed in $time ms")
    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}

suspend fun getMessageOne(): String {
    delay(1000L) //Pretend to do some work
    return "Hello "
}

suspend fun getMessageTwo(): String {
    delay(1000L) //Pretend to do some work
    return "World!"
}




//Sequential Execution

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val time = measureTimeMillis {
        val msgOne = getMessageOne()
        val msgTwo = getMessageTwo()
        println("The Entire Message is: ${msgOne + msgTwo}")
    }

    println("Completed in $time ms")
    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}

suspend fun getMessageOne(): String {
    delay(1000L) //Pretend to do some work
    return "Hello "
}

suspend fun getMessageTwo(): String {
    delay(1000L) //Pretend to do some work
    return "World!"
}



//Timeouts

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val result: String? = withTimeoutOrNull(2000) {
        for (i in 0..500) {
            print("$i.")
            delay(500)
        }
        "I am done"
    }

    println("result: $result")
    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}



//Handling Exceptions

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val job = launch(Dispatchers.Default) { //Thread T1: Create a non blocking coroutine
        try {
            for (i in 0..500) {
                print("$i.")
                delay(5) //or use yield or any other suspending functions as per your need
            }
        } catch (ex: CancellationException) {
            println("\nException caught safely: ${ex.message}")
        } finally {
            withContext(NonCancellable) {
                delay(10) //generally e don't use suspending function in finally
                println("\nClose Resources in finally")
            }
        }

    }

    delay(10) //let us print a few values before we cancel
    job.cancel(CancellationException("My own crash message"))
    job.join()

    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}



//How to make coroutine cooperative 2nd way..

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val job = launch(Dispatchers.Default) { //Thread T1: Create a non blocking coroutine
        for (i in 0..500) {
            if (!isActive) {
                return@launch
                //break
            }
            print("$i.")
            Thread.sleep(1)
        }
    }

    delay(10) //let us print a few values before we cancel
    job.cancelAndJoin()

    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}




fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val job = launch { //Thread T1: Create a non blocking coroutine
        for (i in 0..500) {
            print("$i.")
            yield() // or use delay or any other suspending functions as per our need
            //delay(50)
        }
    }

    delay(3) //let us print a few values before we cancel
    job.cancelAndJoin()
    //job.cancel()
    //job.join() //wait for coroutine to finish

    println("\nMain program ends: ${Thread.currentThread().name}") //Thread: Main
}



//Tenth scenario

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val jobDeferred: Deferred<Int> = async { //Thread: Main    //this child coroutine inherits the parent coroutine scope and hence run on main thread

        println("Fake work starts: ${Thread.currentThread().name}")  //Thread: Main
        delay(1000) //Coroutine is suspend but Thread: Main is free (not blocked)
        println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc
        15
    }

    //delay(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)
    val num : Int = jobDeferred.await()
    //jobDeferred.join()

    println("Main program ends: ${Thread.currentThread().name}") //Thread: Main

}



//Nine scenario

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    val job: Job = launch { //Thread: Main    //this child coroutine inherits the parent coroutine scope and hence run on main thread

        println("Fake work starts: ${Thread.currentThread().name}")  //Thread: Main
        delay(1000) //Coroutine is suspend but Thread: Main is free (not blocked)
        println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

    }

    //delay(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)
    //
    job.join()

    println("Main program ends: ${Thread.currentThread().name}") //Thread: Main

}



//Eight scenario

fun main() = runBlocking {  //create a blocking coroutine that executes in current thread (main)

    println("Main program starts: ${Thread.currentThread().name}") //Thread: Main

    launch { //Thread: Main    //this child coroutine inherits the parent coroutine scope and hence run on main thread

        println("Fake work starts: ${Thread.currentThread().name}")  //Thread: Main
        delay(1000) //Coroutine is suspend but Thread: Main is free (not blocked)
        println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

    }

    delay(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)

    println("Main program ends: ${Thread.currentThread().name}") //Thread: Main

}


//Seven scenario

fun main() = runBlocking {  //Executes on main thread




        println("Main program starts: ${Thread.currentThread().name}") //run on main thread

        GlobalScope.launch { //Let us assume that this is on Thread: T1

            println("Fake work starts: ${Thread.currentThread().name}")  //Thread: T1
            mySuspendFun(1000) //Coroutine is suspend but Thread: T1 is free (not blocked)
            println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

        }

        mySuspendFun(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)

        println("Main program ends: ${Thread.currentThread().name}") //run on main thread

}

suspend fun mySuspendFun(time: Long) {
    //code
    delay(time)
}



//Six scenario

fun main() = runBlocking {  //Executes on main thread


    println("Main program starts: ${Thread.currentThread().name}") //run on main thread

    GlobalScope.launch { //Let us assume that this is on Thread: T1

        println("Fake work starts: ${Thread.currentThread().name}")  //Thread: T1
        delay(1000) //Coroutine is suspend but Thread: T1 is free (not blocked)
        println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

    }

    delay(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)

    println("Main program ends: ${Thread.currentThread().name}") //run on main thread

}



//Fifth Scenario

fun main() = runBlocking { //Creates a coroutine that blocks the current main thread

        println("Main program starts: ${Thread.currentThread().name}") //run on main thread

        GlobalScope.launch { //Let us assume that this is on Thread: T1

            println("Fake work starts: ${Thread.currentThread().name}")  //Thread: T1
            delay(1000) //Coroutine is suspend but Thread: T1 is free (not blocked)
            println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

        }

        delay(2000) //main thread: wait for coroutine to finish (practically not a right way to wait)

        println("Main program ends: ${Thread.currentThread().name}") //run on main thread

}




//Fourth Scenario

fun main() {
        println("Main program starts: ${Thread.currentThread().name}")

        GlobalScope.launch { //Let us assume that this is on Thread: T1

            println("Fake work starts: ${Thread.currentThread().name}")  //Thread: T1
            delay(1000) //Coroutine is suspend but Thread: T1 is free (not blocked)

            println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

        }


        //Creates a coroutine that blocks the current main thread
        runBlocking {
            delay(2000) //wait for coroutine to finish (practically not a right way to wait)
        }

        println("Main program ends: ${Thread.currentThread().name}")

}



//Third Scenario

fun main() {
    println("Main program starts: ${Thread.currentThread().name}")

    GlobalScope.launch { //Let us assume that this is on Thread: T1

        println("Fake work starts: ${Thread.currentThread().name}")  //Thread: T1
        delay(1000) //Coroutine is suspend but Thread: T1 is free (not blocked)

        println("fake work ends: ${Thread.currentThread().name}")//Either T1 or some other Thread T2,T3 etc

    }

    //Blocks the main thread & wait for coroutine to finish (practically not a right way to wait)
    Thread.sleep(2000)

    println("Main program ends: ${Thread.currentThread().name}")

}



//Second Scenario

fun main() {

    println("Main program starts: ${Thread.currentThread().name}")

    GlobalScope.launch { //It creates a background coroutine on background thread

        println("Fake work starts: ${Thread.currentThread().name}")
        Thread.sleep(1000) //Pretend doing some work.. may be file upload

        println("fake work ends: ${Thread.currentThread().name}")

    }

    //Blocks the main thread & wait for coroutine to finish (practically not a right way to wait)
    Thread.sleep(2000)

    println("Main program ends: ${Thread.currentThread().name}")

}



// First Scenario

fun main() {
    
    println("Main program starts: ${Thread.currentThread().name}")

    thread { //It is Background thread create for us (worker thread)

        println("Fake work starts: ${Thread.currentThread().name}")
        Thread.sleep(1000) //Pretend doing some work.. may be file upload

        println("fake work ends: ${Thread.currentThread().name}")

    }

    println("Main program ends: ${Thread.currentThread().name}")

}
