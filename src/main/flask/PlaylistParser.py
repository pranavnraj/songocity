import json
import pandas as pd
import warnings
warnings.filterwarnings('ignore')
from sklearn import preprocessing
from sklearn import svm


def produceTrainingData(user_id):
	data = {}
	with open(user_id + '.txt','r') as test_file:
    	data = json.load(test_file)

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
    return training_data


def produceTestingData(friend_id_list):
	
	test_data = {}
	for id in friend_id_list:
		with open(id + ".txt", 'r') as test_file:
			data = json.load(test_file)
			test_data = test_data + data

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
 	recs = []
	for index, rec in enumerate(preds):
    	if rec[1] >= 0.90 and test_index_to_track[index] not in training_data:
        	recs.append(index)

    track_recs = []
	for index in recs:
    	track_recs.append(test_index_to_track[index])

    track_recs = set(track_recs)

	to_remove = list(set(track_recs) & set(training_data.track))
	for track in to_remove:
    	track_recs.remove(track)

    return track_recs