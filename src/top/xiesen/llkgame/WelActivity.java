package top.xiesen.llkgame;

import top.xiesen.llkgame.other.About;
import top.xiesen.llkgame.view.GameView;
import top.xiesen.llkgame.view.OnStateListener;
import top.xiesen.llkgame.view.OnTimerListener;
import top.xiesen.llkgame.view.OnToolsChangeListener;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.Menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * 欢迎页面Activity
 * @author Allen
 *
 */
public class WelActivity extends Activity implements OnClickListener,
		OnTimerListener, OnStateListener, OnToolsChangeListener {

	private Button btnAbout;
	private ImageButton btnPlay;
	private ImageButton btnRefresh;
	private ImageButton btnTip;
	private ImageView imgTitle;
	private GameView gameView;
	private SeekBar progress;
	private MyDialog dialog;
	private ImageView clock;
	private TextView textRefreshNum;
	private TextView textTipNum;
	private MediaPlayer player;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				dialog = new MyDialog(WelActivity.this, gameView, "胜利！",
						gameView.getTotalTime() - progress.getProgress());
				dialog.show();
				break;
			case 1:
				dialog = new MyDialog(WelActivity.this, gameView, "失败！",
						gameView.getTotalTime() - progress.getProgress());
				dialog.show();
			}
		}
	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		// btnAbout = (Button)findViewById(R.id.about);
		btnPlay = (ImageButton) findViewById(R.id.play_btn);
		btnRefresh = (ImageButton) findViewById(R.id.refresh_btn);
		btnTip = (ImageButton) findViewById(R.id.tip_btn);
		imgTitle = (ImageView) findViewById(R.id.title_img);
		gameView = (GameView) findViewById(R.id.game_view);
		clock = (ImageView) findViewById(R.id.clock);
		progress = (SeekBar) findViewById(R.id.timer);
		textRefreshNum = (TextView) findViewById(R.id.text_refresh_num);
		textTipNum = (TextView) findViewById(R.id.text_tip_num);
		// XXX
		progress.setMax(gameView.getTotalTime());

		btnPlay.setOnClickListener(this);

		btnRefresh.setOnClickListener(this);
		btnTip.setOnClickListener(this);
		gameView.setOnTimerListener(this);
		gameView.setOnStateListener(this);
		gameView.setOnToolsChangedListener(this);
		GameView.initSound(this);

		Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
		imgTitle.startAnimation(scale);
		btnPlay.startAnimation(scale);

		player = MediaPlayer.create(this, R.raw.bg);
		player.setLooping(true);// 设置循环播放
		player.start();

		// GameView.soundPlay.play(GameView.ID_SOUND_BACK2BG, -1);
	}

	@Override
	protected void onPause() {
		super.onPause();
		gameView.setMode(GameView.PAUSE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gameView.setMode(GameView.QUIT);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.play_btn:
			Animation scaleOut = AnimationUtils.loadAnimation(this,
					R.anim.scale_anim_out);
			Animation transIn = AnimationUtils.loadAnimation(this,
					R.anim.trans_in);

			btnPlay.startAnimation(scaleOut);
			btnPlay.setVisibility(View.GONE);
			imgTitle.setVisibility(View.GONE);
			gameView.setVisibility(View.VISIBLE);

			btnRefresh.setVisibility(View.VISIBLE);
			btnTip.setVisibility(View.VISIBLE);
			progress.setVisibility(View.VISIBLE);
			clock.setVisibility(View.VISIBLE);
			textRefreshNum.setVisibility(View.VISIBLE);
			textTipNum.setVisibility(View.VISIBLE);

			btnRefresh.startAnimation(transIn);
			btnTip.startAnimation(transIn);
			gameView.startAnimation(transIn);
			player.pause();
			gameView.startPlay();
			break;
		case R.id.refresh_btn:
			Animation shake01 = AnimationUtils
					.loadAnimation(this, R.anim.shake);
			btnRefresh.startAnimation(shake01);
			gameView.refreshChange();
			break;
		case R.id.tip_btn:
			Animation shake02 = AnimationUtils
					.loadAnimation(this, R.anim.shake);
			btnTip.startAnimation(shake02);
			gameView.autoClear();
			break;
		}
	}

	@Override
	public void onTimer(int leftTime) {
		Log.i("onTimer", leftTime + "");
		progress.setProgress(leftTime);
	}

	@Override
	public void OnStateChanged(int StateMode) {
		switch (StateMode) {
		case GameView.WIN:
			handler.sendEmptyMessage(0);
			break;
		case GameView.LOSE:
			handler.sendEmptyMessage(1);
			break;
		case GameView.PAUSE:
			player.stop();
			gameView.player.stop();
			gameView.stopTimer();
			break;
		case GameView.QUIT:
			player.release();
			gameView.player.release();
			gameView.stopTimer();
			break;
		}
	}

	// 实现退出确认对话框
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("确认退出吗？")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 点击“确认”后的操作
						WelActivity.this.finish();

					}
				})
				.setNegativeButton("返回", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 点击“返回”后的操作,这里不设置没有任何操作
					}
				}).show();
	}

	@Override
	public void onRefreshChanged(int count) {
		textRefreshNum.setText("" + gameView.getRefreshNum());
	}

	@Override
	public void onTipChanged(int count) {
		textTipNum.setText("" + gameView.getTipNum());
	}

	public void quit() {
		this.finish();
	}
}