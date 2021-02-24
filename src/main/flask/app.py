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

import pickle

import PlaylistParser

app = Flask(__name__)

@app.route('/recommend', methods=['GET', 'POST'])
def createRecommendation():

	friend_id_list = request.form.get('friends')
	user_id = request.form.get('user_id')

	training_data = PlaylistParser.produceTrainingData(user_id)
	print(training_data)

	testing_data = PlaylistParser.produceTestingData(friend_id_list)
    print(testing_data)

    test_index_to_track = {}
	for index, row in testing_data.iterrows():
    	test_index_to_track[index] = row['track']

    testing_data_X = testing_data.drop(columns = ['track'])

    pkl_filename = user_id + ".pkl"
    with open(pkl_filename, 'rb') as file:
    	lr = pickle.load(file)

    preds = lr.predict_proba(testing_data_X)

    track_recs = PlaylistParser.produceRecs(preds, test_index_to_track, training_data)
    print(track_recs)

	data = {"friend_ids": track_recs}
	return jsonify(data), 200





