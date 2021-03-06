package com.bignerdranch.android.programming1

import BBallModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bignerdranch.android.gameintent.Game
import com.bignerdranch.android.gameintent.GameDetailViewModel
import com.bignerdranch.android.gameintent.GameInfoModel
import com.bignerdranch.android.gameintent.GameRepository
import java.util.*

private const val REQUEST_CODE_SECOND = 0

private const val ARG_GAME_ID = "id"
private const val TAG = "GameFragment"

class GameFragment: Fragment() {

    interface Callbacks {
        //get only one team's games
        fun onGameListClicked(bool:Boolean)
    }

    var myBBallModel: BBallModel? =  BBallModel()
    var gInfoModel: GameInfoModel? =  GameInfoModel()
    var game : Game = Game()

    private lateinit var threeButton: Button
    private lateinit var twoButton: Button
    private lateinit var ftButton: Button

    private lateinit var threeButtonB: Button
    private lateinit var twoButtonB: Button
    private lateinit var ftButtonB: Button

    private lateinit var resetButton: Button
    private lateinit var scoreA : TextView
    private lateinit var scoreB : TextView

    private lateinit var infoButton : Button
    private  val KEY_SCORE_A = "bundle_score_a"
    private  val KEY_SCORE_B = "bundle_score_b"
    private var savePressed = false

    private lateinit var teamA : EditText
    private lateinit var teamB : EditText

    private lateinit var displayButton : Button
    private lateinit var saveButton : Button

    var teamAWinning :Boolean = false


    private val gameDetailViewModel: GameDetailViewModel by lazy {
        ViewModelProviders.of(this).get(GameDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Fragment Started")
        game = Game()
        //reading from bundle
        val gameId: UUID? = arguments?.getSerializable(ARG_GAME_ID) as? UUID
        if(gameId == null){
            game.teamAName = "Team A"
            game.teamBName = "Team B"
            game.teamAScore = 0
            game.teamBScore = 0
            gameDetailViewModel.addGame(game)

        }else {
            gameDetailViewModel.loadGame(gameId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SCORE_A, myBBallModel!!.scoreA )
        outState.putInt(KEY_SCORE_B, myBBallModel!!.scoreB)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        resetButton = view.findViewById(R.id.button_reset)
        scoreA = view.findViewById(R.id.scoreA)
        scoreB = view.findViewById(R.id.scoreB)

        threeButton = view.findViewById(R.id.three_pointsA)
        threeButtonB =view.findViewById(R.id.three_pointsB)

        twoButton = view.findViewById(R.id.two_pointsA)
        twoButtonB = view.findViewById(R.id.two_pointsB)

        ftButton = view.findViewById(R.id.free_throwA)
        ftButtonB = view.findViewById(R.id.free_throwB)

        infoButton = view.findViewById(R.id.info_button)

        teamA = view.findViewById(R.id.teamA)
        teamB = view.findViewById(R.id.teamB)

        displayButton = view.findViewById(R.id.display_button)
        saveButton = view.findViewById(R.id.save_button)

        if(savedInstanceState != null){
            myBBallModel?.setScore(true,savedInstanceState.getInt(KEY_SCORE_A) )
            myBBallModel?.setScore(false, savedInstanceState.getInt(KEY_SCORE_B))
        }

        resetButton.setOnClickListener {this
            myBBallModel?.resetScore()
            updateScore(true, 0)
            updateScore(false, 0)
        }

        threeButton.setOnClickListener {
            updateScore(true, 3)

        }
        threeButtonB.setOnClickListener {
            updateScore(false, 3)
        }

        twoButton.setOnClickListener {
            updateScore(true, 2)
        }

        twoButtonB.setOnClickListener {
            updateScore(false, 2)
        }

        ftButton.setOnClickListener {
            updateScore(true, 1)
        }

        ftButtonB.setOnClickListener {
            updateScore(false, 1)
        }

        infoButton.setOnClickListener{
            Toast.makeText(context,"If a shot is successfully scored from outside of the three-point line, three points are awarded.\n" +
                    "If a shot is successfully scored from inside of the three-point line, two points are awarded.\n" +
                 "If a team is awarded a technical foul then they will receive between one and three free shots. Each shot scored will be awarded with one point.",
                Toast.LENGTH_SHORT).show()
        }

        displayButton.setOnClickListener{
            //take game score from a and b
            //compare if a > b or other
            if(game.teamAScore > game.teamBScore){
                teamAWinning = true
            }else{
                teamAWinning = false
            }

            (activity as MainActivity).onGameListClicked(teamAWinning)

            gInfoModel?.saveGame(game, teamA.text.toString(), teamB.text.toString(), Integer.parseInt(scoreA.text as String), Integer.parseInt(scoreB.text as String))
            gameDetailViewModel.saveGame(game)

        }

        saveButton.setOnClickListener {
            val intent = SecondActivity.newIntent(activity as MainActivity, savePressed)
            startActivityForResult(intent, REQUEST_CODE_SECOND)

            gInfoModel?.saveGame(game, teamA.text.toString(), teamB.text.toString(), Integer.parseInt(scoreA.text as String), Integer.parseInt(scoreB.text as String))

            Toast.makeText(activity as MainActivity, "Game Information Saved!", Toast.LENGTH_SHORT).show()
        }
        return view }

    fun getIsTeamAWinning() : Boolean{
        return teamAWinning
    }

    private fun updateUI() {
        teamA.setText(game.teamAName)
        teamB.setText(game.teamBName)
        scoreA.text = ((game.teamAScore).toString())
        scoreB.text = ((game.teamBScore).toString())
        myBBallModel?.setScore(true, game.teamAScore)
        myBBallModel?.setScore(false, game.teamBScore)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameDetailViewModel.gameLiveData.observe(
            viewLifecycleOwner,
            Observer{ game ->
                game?.let {
                    this.game = game
                    updateUI() }
            })
    }

    fun updateScore(aBool : Boolean, value :Int ){
        myBBallModel?.addToScore(aBool, value)
        scoreA.text = (myBBallModel?.getScore(true))
        scoreB.text = (myBBallModel?.getScore(false))
    }


    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK){
            return
        }

        if(requestCode == REQUEST_CODE_SECOND){
            myBBallModel?.savePressed = data?.getBooleanExtra(SAVE_BUTTON_KEY, false) ?: false
        }

    }

    override fun onStart() {
        super.onStart()

        val teamAWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                 game.teamAName = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        val teamBWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                game.teamBName = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        teamA.addTextChangedListener(teamAWatcher)
        teamB.addTextChangedListener(teamBWatcher)

    }

    companion object {
        fun newInstance(gameID: UUID): GameFragment {
            val args = Bundle().apply {
                putSerializable(ARG_GAME_ID, gameID)
            }
            return GameFragment().apply {
                arguments = args
            }
        }
    }

    override fun onStop() {
        super.onStop()
        gameDetailViewModel.saveGame(game)
    }
}