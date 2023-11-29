package com.example.a7minuteworkout

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityExerciseBinding
import com.example.a7minuteworkout.databinding.DialogCustomBackConfirmationBinding
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding:ActivityExerciseBinding?=null

    // - Adding a variables for the 10 seconds REST timer
    //START
    private var restTimer: CountDownTimer? = null // Variable for Rest Timer and later on we will initialize it.
    private var restProgress = 0 // Variable for timer progress. As initial value the rest progress is set to 0. As we are about to start.
    //END

    // Adding a variables for the 30 seconds Exercise timer
    // START
    private var exerciseTimer: CountDownTimer? = null // Variable for Exercise Timer and later on we will initialize it.
    private var exerciseProgress = 0 // Variable for the exercise timer progress. As initial value the exercise progress is set to 0. As we are about to start.
    // END
    private var exerciseTimerDuration:Long=30

    // The Variable for the exercise list and current position of exercise here it is -1 as the list starting element is 0
    // START
    private var exerciseList: ArrayList<ExerciseModel>? = null // We will initialize the list later.
    private var currentExercisePosition = -1 // Current Position of Exercise.
    // END

    private var tts:TextToSpeech?=null
   // private var player: MediaPlayer? = null

    private var exerciseAdapter: ExerciseStatusAdapter?=null



    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        //inflate the layout
        binding= ActivityExerciseBinding.inflate(layoutInflater)
        // pass in binding?.root in the content view
        setContentView(binding?.root)
        // then set support action bar and get toolBarExerciser using the binding variable
        setSupportActionBar(binding?.toolbarExercise)


        if (supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarExercise?.setNavigationOnClickListener{
            customDialogForBackButton()
        }
        tts= TextToSpeech(this,this)
        exerciseList=Constants.defaultExerciseList()
        setupRestView()
        //here we are calling this function after exerciseList=Constants.defaultExerciseList()
        //so the exercise list will have the list of exercise and we will not get any error.
        setUpExerciseStatusRecyclerView()
    }

    private fun setUpExerciseStatusRecyclerView(){
       binding?.rvExerciseStatus?.layoutManager=
           LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

       exerciseAdapter=ExerciseStatusAdapter(exerciseList!!)
       binding?.rvExerciseStatus?.adapter=exerciseAdapter
   }
    //Setting up the Get Ready View with 10 seconds of timer
    //START
    /**
     * Function is used to set the timer for REST.
     */
    private fun setupRestView() {
//        try{
//            val soundURI= Uri.parse(
//                "android.resource://com.example.a7minuteworkout/"+R.raw.app_src_main_res_raw_press_start)
//            player=MediaPlayer.create(applicationContext,soundURI)
//            player?.isLooping=false
//            player?.start()
//        }catch (e:Exception){
//            e.printStackTrace()
//        }

        /**
         * here we are hiding the exercise layout and making the rest timer visible.
         */
        speakOut("GET READY FOR THE EXERCISE.")
        binding?.flRestView?.visibility=View.VISIBLE
        binding?.tvTitle?.visibility=View.VISIBLE
        binding?.tvExerciseName?.visibility=View.INVISIBLE
        binding?.ivImage?.visibility=View.INVISIBLE
        binding?.flExerciseView?.visibility=View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility=View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility=View.VISIBLE

        speakOut("Please rest")
        /**
         * Here firstly we will check if the timer is running the and it is not null then cancel the running timer and start the new one.
         * And set the progress to initial which is 0.
         */
        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }


        binding?.tvUpcomingExerciseName?.text= exerciseList!![currentExercisePosition+1].getName()
        setRestProgressBar()
    }



    private fun setRestProgressBar() {
        binding?.progressBar?.progress=restProgress// Sets the current progress to the specified value.

        /**
         * //@param millisInFuture The number of millis in the future from the call
         *   to {#start()} until the countdown is done and {#onFinish()}
         *   is called.
         * //@param countDownInterval The interval along the way to receive
         *   {#onTick(long)} callbacks.
         */
        // Here we have started a timer of 10 seconds so the 10000 is milliseconds is 10 seconds and the countdown interval is 1 second so it 1000.
        restTimer=object:CountDownTimer(10000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++//it is increased by 1
                binding?.progressBar?.progress=10-restProgress// Indicates progress bar progress
                binding?.tvTimer?.text=(10-restProgress).toString()// Current progress is set to text view in terms of seconds.
            }

            override fun onFinish() {
                // When the 10 seconds will complete this will be executed.
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()
                setupExerciseView()
            }
        }.start()
    }
    //END


    // Setting up the Exercise View with a 30 seconds timer
    // START
    /**
     * Function is used to set the progress of the timer using the progress for Exercise View.
     */
    private fun setupExerciseView(){
        /**
         * here we are hiding the rest timer layout and making the exercise layout visible.
         */
        binding?.flRestView?.visibility=View.INVISIBLE
        binding?.tvTitle?.visibility=View.INVISIBLE
        binding?.tvExerciseName?.visibility=View.VISIBLE
        binding?.ivImage?.visibility=View.VISIBLE
        binding?.flExerciseView?.visibility=View.VISIBLE
        binding?.tvUpcomingLabel?.visibility=View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility=View.INVISIBLE


        /**
         * Here firstly we will check if the timer is running and it is not null then cancel the running timer and start the new one.
         * And set the progress to the initial value which is 0.
         */
        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress=0
        }
        speakOut(exerciseList!![currentExercisePosition].getName())

        // Setting up the current exercise name and imageview to the UI element.
        // START
        /**
         * Here current exercise name and image is set to exercise view.
         */
        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text=exerciseList!![currentExercisePosition].getName()
        // END
        setExerciseProgressBar()
    }
    // END


    // After REST View Setting up the 30 seconds timer for the Exercise view and updating it continuously
    // START
    /**
     * Function is used to set the progress of the timer using the progress for Exercise View for 30 Seconds
     */

    private fun setExerciseProgressBar() {
        binding?.progressBarExercise?.progress=exerciseProgress
        exerciseTimer=object :CountDownTimer(exerciseTimerDuration*1000,1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress=exerciseTimerDuration.toInt()-exerciseProgress
                binding?.tvTimerExercise?.text=(exerciseTimerDuration.toInt()-exerciseProgress).toString()
            }
            override fun onFinish() {

                if(currentExercisePosition<exerciseList?.size!!-1){
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                }else{
                    finish()
                    val intent= Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }
        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress=0
        }

        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }
//        if(player!=null){
//            player!!.stop()
//        }



        binding=null
    }

    override fun onInit(status: Int) {
        if (status==TextToSpeech.SUCCESS){
            val result=tts?.setLanguage(Locale.ENGLISH)
            if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","The Language is not supported!")
            }
        }else{
            Log.e("TTS","Initialization Failed.")
        }
    }

    private fun speakOut(text: String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    private fun customDialogForBackButton() {

        val customDialog=Dialog(this)
        val dialogBinding=DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }


    override fun onBackPressed() {
        customDialogForBackButton()
    }

}