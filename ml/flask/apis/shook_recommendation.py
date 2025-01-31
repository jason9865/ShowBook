from flask import Blueprint,request
from flask_restx import Api
from flask import Blueprint, make_response, jsonify
from utils.shook_util import get_shook_like, get_df,get_user_similarity \
                            ,get_compare_matrix,get_similar_summarize,get_shook_list, get_shooks \
                            ,get_random_shooks

from time import time

shook_recommendation = Blueprint("shook_recommendation",__name__)
api = Api(shook_recommendation)

@shook_recommendation.route("/recommend",methods=["GET"])
def getShooks():
    member_id = int(request.args.get('member_id'))
    page = request.args.get('page', default=1, type=int)
    
    shooks_list = []
    
    #1. member_id를 받으면 member_id랑 매칭되는 shook_like 정보 리턴
    member_df = get_shook_like(member_id)
    
    if (len(member_df) < 3):
        # member의 shook_like 개수가 3개 미만인 경우 
        # 인기 순으로 슈욱을 들고 온다
        print("====================1. get_random_shook====================")
        shooks_list = get_random_shooks(page)

    else: 
        print("====================2. get 좋아요한 shook과 유사한 shook====================")
        #2. 1의 결과를 바탕으로 shook 행렬 반환
        df_pivoted = get_df(member_df,member_id)
        
        #3. 2의 결과물을 받고 가장 유사한 사람의 수 추려오는 함수
        similar_list = get_user_similarity(df_pivoted, member_id)
        
        #4. similar_list 받아서 compare matrix 리턴
        compare_matrix = get_compare_matrix(similar_list)
        
        #5. 유사도 가중치가 부여된 값 생성
        similar_summarize = get_similar_summarize(compare_matrix, similar_list)
        
        #6. 추천할 shook_id_list 반환
        sorted_index = get_shook_list(similar_summarize,df_pivoted)
        
        #7. 추천할 shook 반환
        shooks_list = get_shooks(sorted_index)
    
    response = {
        "success" : True,
        "data" : shooks_list
    }
    
    return make_response(jsonify(response),200)
    
