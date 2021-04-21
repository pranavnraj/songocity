from flask import Flask
from flask import request
from flask import jsonify

import json
import pandas as pd
from sklearn.linear_model import LogisticRegressionCV
from sklearn.model_selection import cross_val_score
import warnings
warnings.filterwarnings('ignore')
from sklearn import preprocessing
from sklearn import svm
import boto3
import tempfile
import pickle

import PlaylistParser

application = Flask(__name__)

@application.route('/ping', methods=['GET', 'POST'])
def ping():
	return jsonify("hello there"),200

@application.route('/recommend', methods=['GET', 'POST'])
def createRecommendation():

	friend_id_list = request.get_json()['friends']
	user_id = request.get_json()['user_id']

	training_data, features = PlaylistParser.produceTrainingData(user_id)
	#print(type(training_data))

	#print("BREAK")

	testing_data = PlaylistParser.produceTestingData(friend_id_list)
	#print(testing_data)

	test_index_to_track = {}
	for index, row in testing_data.iterrows():
		test_index_to_track[index] = row['track']

	testing_data_X = testing_data.drop(columns = ['track'])

	s3 = boto3.client('s3')
	bucket = 'songbirdsdata'
	key = 'Models/' + user_id + '.pkl'

	with tempfile.TemporaryFile() as fp:
		s3.download_fileobj(Fileobj=fp, Bucket=bucket, Key=key)
		fp.seek(0)
		lr = pickle.load(fp)

	#pkl_filename = user_id + ".pkl"
	#with open(pkl_filename, 'rb') as file:
	#	lr = pickle.load(file)

	preds = lr.predict_proba(testing_data_X)

	track_recs = PlaylistParser.produceRecs(preds, test_index_to_track, training_data)
	print("Track recs")
	print(track_recs)

	data = {"friendIDs": list(track_recs)}
	return jsonify(data), 200

@application.route('/train', methods=['GET', 'POST'])
def trainModel():

	#user_id = request.form.get('user_id')
	user_id = request.get_json()['user_id']
	#print(user_id)

	training_data, features = PlaylistParser.produceTrainingData(user_id)
	#print(training_data)

	#print("BREAK")

	genre_training_data = PlaylistParser.produceGenreTrainingData(user_id, training_data, features)
	#print(genre_training_data)

	combined_training_data = pd.concat([training_data,genre_training_data],ignore_index=True)

	index_to_track = {}
	for index, row in combined_training_data.iterrows():
		index_to_track[index] = row['track']

	X = combined_training_data.drop(['track','inPlaylist'], axis=1)
	y = combined_training_data['inPlaylist']
	lr = LogisticRegressionCV(cv=5, random_state=42).fit(X, y)

	s3 = boto3.client('s3')
	bucket = 'songbirdsdata'
	key = 'Models/' + user_id + '.pkl'

	with tempfile.TemporaryFile() as fp:
		pickle.dump(lr, fp)
		fp.seek(0)
		s3.put_object(Body=fp.read(), Bucket=bucket, Key=key)


	return jsonify(lr.score(X, y)),200

if __name__ == '__main__':
	application.run(port=5001, debug=True)



