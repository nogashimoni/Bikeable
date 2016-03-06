from __future__ import print_function # Python 2/3 compatibility
import ast
from datetime import datetime
from urllib2 import urlopen
import boto3

// This code runs every 5 minutes. It retrieves the data from tel-o-fun website, parses it,
// and updates dynamoDB telOFun table.


TELOFUN_SITE = 'https://www.tel-o-fun.co.il/en/TelOFunLocations.aspx'  # URL of the tel-o-fun site
DB_URL = "https://dynamodb.eu-west-1.amazonaws.com"


def get_site_content(site):
    try:
        url_content = urlopen(site).read()
        return url_content
    except:
        print('url open failed!')
        raise
    finally:
        print('Check complete at {}\n'.format(str(datetime.now())))


def parse_telofun_data(telofun_data):
    indexOfSectionStart = telofun_data.find("setMarker") + len("setMarker")
    indexOfSectionEnd = telofun_data.find("</script>", indexOfSectionStart)
    relevantSection = telofun_data[indexOfSectionStart:indexOfSectionEnd]
    stationsDataLines = relevantSection.split("setMarker")

    listOfStationsData = []
    for line in stationsDataLines:
        relevantStationsDataArr = line.split(', \'<a href')
        relevantStationsData = relevantStationsDataArr[0] + ')'
        stationTuple = ast.literal_eval(relevantStationsData)
        listOfStationsData.append(stationTuple)

    return listOfStationsData



def loadStationsToTable (listOfStations):

    dynamodb = boto3.resource('dynamodb', endpoint_url=DB_URL)

    table = dynamodb.Table('TelOFun')

    time = str (datetime.utcnow())

    for stationData in listOfStations:
        id = int(stationData[2])
        bikesAvailable = int(stationData[5]) - int(stationData[6])
        standsAvailable = int(stationData[6])

        table.put_item(
            Item={
                 'StationID': id,
                 'Name': stationData[3],
                 'BikesAvailable': bikesAvailable,
                 'StandsAvailable': standsAvailable,
                 'TimeStamp' : time
            }
        )


def getTelOFunData():
    site_content = get_site_content(TELOFUN_SITE)
    telofun_data = parse_telofun_data(site_content)
    loadStationsToTable(telofun_data)


def lambda_handler(event, context):
    print('Checking at {}...'.format(str(datetime.now())))
    try:
        getTelOFunData()
    except:
        print('Data load failed!')
        raise
    else:
        print('Check passed!')
    finally:
        print('Check complete at {}'.format(str(datetime.now())))
