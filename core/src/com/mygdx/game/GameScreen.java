package com.mygdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;
import java.util.Random;

public class GameScreen implements Screen {
	final Drop game;
	SpriteBatch batch;
	Texture background;
	Texture bucketImage;
	Array<Texture> objectClassTexture = new Array<>();
	Sound caughtSound;
    Sound escapeSound;
	Music music;
	OrthographicCamera camera;
	Rectangle gg;
	Array<Raindrop> raindrops = new Array<>();
	long lastDropTime;
	int caughtAstus = 0;
	int caughtEuclides = 0;
	int caughtKeters = 0;
	int escapedAstus = 0;
	int escapedEuclides = 0;
	int escapedKeters = 0;
    int socialCredits = 0;
	long startCurrentTime;
	long finishCurrentTime;


	public GameScreen(final Drop gam) {
		this.game = gam;

		background = new Texture("pictures/background.jpg");
		bucketImage = new Texture(Gdx.files.internal("pictures/gg.png"));
		objectClassTexture.add(new Texture(Gdx.files.internal("pictures/astus.jpg")));
		objectClassTexture.add(new Texture(Gdx.files.internal("pictures/euclid.jpg")));
		objectClassTexture.add(new Texture(Gdx.files.internal("pictures/keter.jpg")));
		caughtSound = Gdx.audio.newSound(Gdx.files.internal("sounds/woo.wav"));
        escapeSound = Gdx.audio.newSound(Gdx.files.internal("sounds/fuck.wav"));
		music = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.mp3"));

		music.setLooping(true);

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

        spawnAgent();
		spawnRaindrop();

		startCurrentTime = System.currentTimeMillis();
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.batch.draw(background, 0, 0);

        game.font.draw(game.batch, "Social credits: " + socialCredits, 0, 480);
        game.font.draw(game.batch, "Caught safes: " + caughtAstus, 0, 440);
		game.font.draw(game.batch, "Caught euclides: " + caughtEuclides, 0, 420);
		game.font.draw(game.batch, "Caught keters: " + caughtKeters, 0, 400);
		game.font.draw(game.batch, "Escaped safes: " + escapedAstus, 0, 380);
		game.font.draw(game.batch, "Escaped euclides: " + escapedEuclides, 0, 360);
		game.font.draw(game.batch, "Escaped keters: " + escapedKeters, 0, 340);

		game.batch.draw(bucketImage, gg.x, gg.y);

		for(Raindrop raindrop: raindrops) {
			game.batch.draw(raindrop.texture, raindrop.x, raindrop.y);
		}

		game.batch.end();

		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			gg.x = touchPos.x - 32;
		}

		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			gg.x -= 500 * Gdx.graphics.getDeltaTime();}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			gg.x += 500 * Gdx.graphics.getDeltaTime();}

		if(gg.x < 0){
			gg.x = 0;}
		if(gg.x > 800 - 64){
			gg.x = 800 - 64;}

		if(TimeUtils.nanoTime() - lastDropTime > 1000000000){
			spawnRaindrop();}

		Iterator<Raindrop> iterator = raindrops.iterator();
		while(iterator.hasNext()) {
			Raindrop raindrop = iterator.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0){
				switch (raindrop.type){
					case "safe":
						escapedAstus++;
						break;
					case "euclid":
						escapedEuclides++;
						break;
					case "keter":
						escapedKeters++;
						break;
				}
                escapeSound.play();
				iterator.remove();
			}
			if(raindrop.overlaps(gg)) {
				switch (raindrop.type){
					case "safe":
						caughtAstus++;
						break;
					case "euclid":
						caughtEuclides++;
						break;
					case "keter":
						caughtKeters++;
						break;
				}
				caughtSound.play();
				iterator.remove();
			}
		}
		calculateSocialCredits();

		finishCurrentTime = System.currentTimeMillis();

		if (finishCurrentTime - startCurrentTime > 110000){
			raindrops.clear();
			music.stop();
			objectClassTexture.clear();
			game.setScreen(new EndGameScreen(game, socialCredits));
		}
	}
	
	@Override
	public void dispose () {
		bucketImage.dispose();
		caughtSound.dispose();
		music.dispose();
		batch.dispose();
		background.dispose();
	}

	private void spawnRaindrop() {
		Raindrop raindrop = new Raindrop();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;

		Random random = new Random();
		int randomObjectTexture = random.nextInt(3);
		switch (randomObjectTexture){
			case 0:
				raindrop.texture = objectClassTexture.get(0);
				raindrop.type = "safe";
				break;
			case 1:
				raindrop.texture = objectClassTexture.get(1);
				raindrop.type = "euclid";
				break;
			case 2:
				raindrop.texture = objectClassTexture.get(2);
				raindrop.type = "keter";
				break;
		}
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

    private void spawnAgent() {
        gg = new Rectangle();
        gg.x = 400 - 32;
        gg.y = 20;
        gg.width = 155;
        gg.height = 64;
    }

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		music.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	private void calculateSocialCredits(){

        socialCredits = caughtAstus + caughtEuclides*10 + caughtKeters*25 -
                        escapedAstus*10 - escapedEuclides*25 - escapedKeters*100;
    }
}
