from flask import Flask
from flask import request
from flask import jsonify

app = Flask(__name__)

@app.route('/recommend', methods=['GET', 'POST'])
def hello_world():

	friend_id_list = request.form.get('friends')
	print(friend_id_list)
	data = {"response": "successful"}
	return jsonify(data), 200





