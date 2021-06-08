import json
import pandas as pd
import warnings
import boto3
warnings.filterwarnings('ignore')
from sklearn import preprocessing
from sklearn import svm


def produceTrainingData(user_id):

	bucket = 'songbirdsdata'
	key = 'UserSongs/' + user_id + '.txt'
	print(key)

	s3 = boto3.resource('s3')
	obj = s3.Object(bucket, key)
	json_data = obj.get()['Body'].read().decode('utf-8')
	data = json.loads(json_data)

	#data = {}
	#with open('../jupyter_notebooks/' + user_id + '.txt','r') as test_file:
	#	data = json.load(test_file)

	#print(data)

	for playlist in data:
		for song in data[playlist]:
			features = list(data[playlist][song].keys())
			break

	features.insert(0,"track")
	features.append("inPlaylist")

	rows = []

	for playlist in data:
		for song in data[playlist]:
			row = [song]
			for feature in data[playlist][song]:
				row.append(data[playlist][song][feature])
			row.append(1)
			rows.append(row)

	training_data = pd.DataFrame(rows, columns = features)
	return training_data, features

def produceGenreTrainingData(user_id, training_data, features):
	bucket = 'songbirdsdata'
	key = 'GenreSongs/' + user_id + 'genres.txt'

	s3 = boto3.resource('s3')
	obj = s3.Object(bucket, key)
	json_data = obj.get()['Body'].read().decode('utf-8')
	data = json.loads(json_data)

	#data = {}

	#with open('../jupyter_notebooks/' + user_id + 'genres.txt', 'r') as genres_file:
	#	data = json.load(genres_file)

	rows = []

	for song in data:
		row = [song]
		for feature in data[song]:
			row.append(data[song][feature])
		row.append(0)
		rows.append(row)

	genre_training_data = pd.DataFrame(rows, columns = features)

	commonSongs = list(set(training_data.track) & set(genre_training_data.track))
	genre_training_data.loc[genre_training_data['track'].isin(commonSongs),"inPlaylist"] = 1

	return genre_training_data


def produceTestingData(friend_id_list):
	
	bucket = 'songbirdsdata'
	s3 = boto3.resource('s3')

	test_data = {}
	for id in friend_id_list:
		key = 'UserSongs/' + id + '.txt'
		obj = s3.Object(bucket, key)

		try:
			json_data = obj.get()['Body'].read().decode('utf-8')
		except s3.meta.client.exceptions.NoSuchKey:
			return "NoSuchKey in Bucket(friend test data is not in bucket)"

		data = json.loads(json_data)
		test_data.update(data)

		#with open('../jupyter_notebooks/' + id + ".txt", 'r') as test_file:
		#	data = json.load(test_file)
		#	test_data.update(data)

	for playlist in test_data:
		for song in test_data[playlist]:
			features = list(test_data[playlist][song].keys())
			break

	features.insert(0, "track")

	rows = []

	for playlist in test_data:
		for song in test_data[playlist]:
			row = [song]
			for feature in test_data[playlist][song]:
				row.append(test_data[playlist][song][feature])
			rows.append(row)

	testing_data = pd.DataFrame(rows, columns = features)
	return testing_data

def produceRecs(preds, test_index_to_track, training_data):

	sort_preds = [row[1] for row in preds]
	sort_preds.sort(reverse=True)
	if (len(sort_preds) <= 25):
		threshold = sort_preds[len(sort_preds) - 1]
	else:
		threshold = sort_preds[24]

	recs = []
	for index, rec in enumerate(preds):
		if rec[1] >= threshold and test_index_to_track[index] not in training_data:
			recs.append(index)

	track_recs = []
	for index in recs:
		track_recs.append(test_index_to_track[index])

	track_recs = set(track_recs)

	#print(training_data)

	to_remove = list(set(track_recs) & set(training_data.track))
	for track in to_remove:
		track_recs.remove(track)

	return track_recs