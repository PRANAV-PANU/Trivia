package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView questionCounter,questionTextView,scoreTextView,highScoreTextView;
    private Button trueButton,falseButton;
    private ImageButton prevButton,nextButton;
    private int currentQuestionIndex;
    private List<Question> questionList;
    private Prefs prefs;

    private int scoreCounter=0;
    private Score score;

    //link - https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();
        prefs = new Prefs(MainActivity.this);

        currentQuestionIndex = prefs.getState();

        highScoreTextView = findViewById(R.id.highestScore);
        scoreTextView=findViewById(R.id.score_textView);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_Button);
        trueButton = findViewById(R.id.trueButton);
        falseButton = findViewById(R.id.falseButton);
        questionTextView = findViewById(R.id.questionTextView);
        questionCounter = findViewById(R.id.counter_text);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);

        highScoreTextView.setText(MessageFormat.format("High Score : {0}", String.valueOf(prefs.getHighScore())));

        questionList=new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                questionTextView.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounter.setText(currentQuestionIndex +" / "+ questionArrayList.size());// 0/100
                Log.d("inside", "processFinished: "+questionArrayList);
            }
        });
        //Log.d("MAIN", "onCreate: "+questionList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.prev_Button:
                if(currentQuestionIndex == 0){
                    currentQuestionIndex = questionList.size();
                }else{
                    currentQuestionIndex=(currentQuestionIndex-1)%questionList.size();
                    updateQuestion();
                }
                break;

            case R.id.next_button:
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
                break;

            case R.id.trueButton:
                checkAnswer(true);
                updateQuestion();
                break;

            case R.id.falseButton:
                checkAnswer(false);
                updateQuestion();
                break;
        }
    }

    private void checkAnswer(boolean b) {
        int ToastMessageID;
        if(b == questionList.get(currentQuestionIndex).isAnswerTrue()){
            fadeView();
            addPoints();
            ToastMessageID=R.string.correct_answer;
        }else {
            shakeAnimation();
            deductPoints();
            ToastMessageID=R.string.wrong_answer;
        }
        Toast.makeText(getApplicationContext(), ToastMessageID, Toast.LENGTH_SHORT).show();
    }

    private void deductPoints() {
        scoreCounter -=100;
        if(scoreCounter<0){
            scoreCounter=0;
            score.setScore(scoreCounter);
        }else{
            score.setScore(scoreCounter);
        }
        scoreTextView.setText(MessageFormat.format("Current Score : {0}", String.valueOf(score.getScore())));
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        scoreTextView.setText(MessageFormat.format("Current Score : {0}", String.valueOf(score.getScore())));
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView2);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextView.setText(question);
        questionCounter.setText(currentQuestionIndex+" / "+questionList.size());
    }

    private void fadeView(){
        final CardView cardView = findViewById(R.id.cardView2);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                currentQuestionIndex = (currentQuestionIndex+1)%questionList.size();
                updateQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        prefs.setState(currentQuestionIndex);
        prefs.saveHighScore(score.getScore());
        super.onPause();
    }
}

